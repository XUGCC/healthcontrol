package com.example.web.service.front.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictInput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.LocalPredictionOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalysisOutput;
import com.example.web.dto.front.LaryngoscopeAnalysisDtos.QwenAnalyzeInput;
import com.example.web.entity.TLaryngoscopeLocalPredictionRecord;
import com.example.web.entity.TLaryngoscopePhoto;
import com.example.web.entity.TLaryngoscopeQwenAnalysisRecord;
import com.example.web.mapper.TLaryngoscopeLocalPredictionRecordMapper;
import com.example.web.mapper.TLaryngoscopePhotoMapper;
import com.example.web.mapper.TLaryngoscopeQwenAnalysisRecordMapper;
import com.example.web.service.front.FrontLaryngoscopeAnalysisService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.ai.QwenBailianClient;
import com.example.web.tools.dto.IdInput;
import com.example.web.tools.exception.CustomException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class FrontLaryngoscopeAnalysisServiceImpl implements FrontLaryngoscopeAnalysisService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String QWEN_REPORT_TYPE = "LARYNGOSCOPE_IMAGE_ANALYSIS";
    private static final String QWEN_PROMPT_VERSION = "laryngoscope-image-v1";
    private static final String QWEN_MODEL_NAME = "qwen-vl-plus";
    private static final String PROVIDER = "DashScope";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private TLaryngoscopePhotoMapper photoMapper;

    @Autowired
    private TLaryngoscopeLocalPredictionRecordMapper localPredictionMapper;

    @Autowired
    private TLaryngoscopeQwenAnalysisRecordMapper qwenAnalysisMapper;

    @Autowired
    private QwenBailianClient qwenClient;

    @Value("${laryngoscope.local-prediction.python-path:python}")
    private String pythonPath;

    @Value("${laryngoscope.local-prediction.script-path:laryngoscope prediction/predict_larynscope.py}")
    private String scriptPath;

    @Value("${laryngoscope.local-prediction.model-path:laryngoscope prediction/repalexnet_heatmap_n100.pth}")
    private String modelPath;

    @Value("${laryngoscope.local-prediction.output-root:external-resources/laryngoscope-predictions}")
    private String outputRoot;

    @Value("${laryngoscope.local-prediction.device:auto}")
    private String device;

    @Value("${laryngoscope.local-prediction.timeout-seconds:120}")
    private Integer timeoutSeconds;

    @Override
    public LocalPredictionOutput localPredict(LocalPredictInput input) {
        if (input == null || input.getId() == null || input.getId() <= 0) {
            throw new CustomException("缺少喉镜照片Id");
        }
        Integer userId = requireUserId();
        TLaryngoscopePhoto photo = requirePhoto(input.getId(), userId);
        boolean force = Boolean.TRUE.equals(input.getForceRegenerate());
        if (!force) {
            TLaryngoscopeLocalPredictionRecord latest = findLatestLocalSuccess(userId, photo.getId());
            if (latest != null) {
                return toLocalOutput(latest);
            }
        }

        TLaryngoscopeLocalPredictionRecord record = createLocalRecord(userId, photo);
        long start = System.nanoTime();
        try {
            File sourceFile = resolveLocalFileFromUrl(photo.getLaryngoscopePhotoUrl());
            File repoRoot = resolveRepoRoot();
            File scriptFile = resolveFile(scriptPath, repoRoot);
            File modelFile = resolveModelFile(repoRoot);
            if (!scriptFile.exists()) {
                throw new LaryngoscopeFailure("LOCAL_MODEL_CONFIG_MISSING", "找不到本地预测脚本：" + scriptFile.getAbsolutePath());
            }
            if (!modelFile.exists()) {
                throw new LaryngoscopeFailure("LOCAL_MODEL_CONFIG_MISSING", "找不到本地模型权重：" + modelFile.getAbsolutePath());
            }

            File runDir = buildPredictionRunDir(photo.getId());
            if (!runDir.exists() && !runDir.mkdirs()) {
                throw new LaryngoscopeFailure("LOCAL_MODEL_PROCESS_FAILED", "创建预测输出目录失败：" + runDir.getAbsolutePath());
            }

            runPredictionProcess(sourceFile, scriptFile, modelFile, runDir);
            JsonNode payload = readPredictionPayload(new File(runDir, "predictions.json"));
            JsonNode prediction = payload.path("records").isArray() && payload.path("records").size() > 0
                    ? payload.path("records").get(0)
                    : null;
            if (prediction == null || prediction.isMissingNode()) {
                throw new LaryngoscopeFailure("LOCAL_MODEL_OUTPUT_INVALID", "本地模型输出中缺少 records[0]");
            }

            File heatmapFile = resolveHeatmapFile(prediction.path("heatmap_path").asText(""), runDir, repoRoot);
            if (heatmapFile == null || !heatmapFile.exists()) {
                throw new LaryngoscopeFailure("LOCAL_HEATMAP_MISSING", "热力图文件缺失");
            }

            int durationMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            record.setUpdateTime(LocalDateTime.now());
            record.setSourcePhotoLocalPath(sourceFile.getAbsolutePath());
            record.setPredictionStatus(STATUS_SUCCESS);
            record.setPredictedClassId(prediction.path("predicted_class_id").isMissingNode() ? null : prediction.path("predicted_class_id").asInt());
            record.setPredictedLabel(prediction.path("predicted_label").asText(null));
            record.setConfidence(prediction.path("confidence").isMissingNode() ? null : prediction.path("confidence").asDouble());
            record.setProbabilitiesJson(mapper.writeValueAsString(prediction.path("probabilities")));
            record.setHeatmapLocalPath(heatmapFile.getAbsolutePath());
            record.setHeatmapUrl(toExternalResourceUrl(heatmapFile));
            record.setModelName("RepAlexNet");
            record.setModelVersion("repalexnet_heatmap_n100");
            record.setModelPath(modelFile.getAbsolutePath());
            record.setScriptPath(scriptFile.getAbsolutePath());
            record.setRuntimeEnv(pythonPath + " " + device);
            record.setRawOutputJson(mapper.writeValueAsString(payload));
            record.setErrorCode(null);
            record.setErrorMessage(null);
            record.setDurationMs(durationMs);
            localPredictionMapper.updateById(record);
            return toLocalOutput(record);
        } catch (LaryngoscopeFailure ex) {
            return failLocal(record, ex.code, ex.getMessage(), start);
        } catch (Exception ex) {
            return failLocal(record, "LOCAL_MODEL_PROCESS_FAILED", ex.getMessage(), start);
        }
    }

    @Override
    public LocalPredictionOutput getLocalPrediction(IdInput input) {
        if (input == null || input.getId() == null || input.getId() <= 0) {
            throw new CustomException("缺少喉镜照片Id");
        }
        Integer userId = requireUserId();
        requirePhoto(input.getId(), userId);
        TLaryngoscopeLocalPredictionRecord latest = findLatestLocal(userId, input.getId());
        return latest == null ? new LocalPredictionOutput() : toLocalOutput(latest);
    }

    @Override
    public QwenAnalysisOutput qwenAnalyze(QwenAnalyzeInput input) {
        if (input == null || input.getId() == null || input.getId() <= 0) {
            throw new CustomException("缺少喉镜照片Id");
        }
        Integer userId = requireUserId();
        TLaryngoscopePhoto photo = requirePhoto(input.getId(), userId);
        boolean force = Boolean.TRUE.equals(input.getForceRegenerate());
        boolean includeLocal = Boolean.TRUE.equals(input.getIncludeLocalPredictionContext());

        if (!force) {
            TLaryngoscopeQwenAnalysisRecord latest = findLatestQwenSuccess(userId, photo.getId(), includeLocal);
            if (latest != null) {
                return toQwenOutput(latest);
            }
        }

        TLaryngoscopeQwenAnalysisRecord record = createQwenRecord(userId, photo, includeLocal);
        long start = System.nanoTime();
        try {
            File sourceFile = resolveLocalFileFromUrl(photo.getLaryngoscopePhotoUrl());
            String localSnapshot = null;
            Integer localPredictionId = null;
            if (includeLocal) {
                TLaryngoscopeLocalPredictionRecord local = findLatestLocalSuccess(userId, photo.getId());
                if (local != null) {
                    localPredictionId = local.getId();
                    localSnapshot = buildLocalPredictionSnapshot(local);
                }
            }

            String prompt = buildLaryngoscopePrompt(photo, includeLocal, localSnapshot);
            String imageBase64 = Base64.getEncoder().encodeToString(Files.readAllBytes(sourceFile.toPath()));
            QwenBailianClient.QwenResponse resp = qwenClient.chatWithImageBase64(prompt, imageBase64, resolveImageMimeType(sourceFile));
            int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            QwenParse parse = parseQwenReport(resp.contentText());

            record.setUpdateTime(LocalDateTime.now());
            record.setSourcePhotoLocalPath(sourceFile.getAbsolutePath());
            record.setLocalPredictionRecordId(localPredictionId);
            record.setLocalPredictionSnapshotJson(localSnapshot);
            record.setRawRequest(prompt);
            record.setRawResponse(resp.rawResponse());
            record.setLatencyMs(latencyMs);
            record.setParseStatus(parse.parseStatus);
            record.setRiskLevel(parse.riskLevel);
            record.setSummaryText(parse.summaryText);
            record.setReportJson(parse.reportJson);
            if (STATUS_SUCCESS.equals(parse.parseStatus)) {
                record.setAnalysisStatus(STATUS_SUCCESS);
                record.setErrorCode(null);
                record.setErrorMessage(null);
            } else {
                record.setAnalysisStatus(STATUS_FAILED);
                record.setErrorCode("QWEN_JSON_PARSE_FAILED");
                record.setErrorMessage(parse.errorMessage);
            }
            qwenAnalysisMapper.updateById(record);
            return toQwenOutput(record);
        } catch (Exception ex) {
            return failQwen(record, "QWEN_CALL_FAILED", ex.getMessage(), start);
        }
    }

    @Override
    public QwenAnalysisOutput getQwenAnalysis(IdInput input) {
        if (input == null || input.getId() == null || input.getId() <= 0) {
            throw new CustomException("缺少喉镜照片Id");
        }
        Integer userId = requireUserId();
        requirePhoto(input.getId(), userId);
        TLaryngoscopeQwenAnalysisRecord latest = findLatestQwen(userId, input.getId());
        return latest == null ? new QwenAnalysisOutput() : toQwenOutput(latest);
    }

    public LocalPredictionOutput toLocalOutput(TLaryngoscopeLocalPredictionRecord entity) {
        LocalPredictionOutput out = new LocalPredictionOutput();
        if (entity == null) return out;
        out.setPredictionId(entity.getId());
        out.setLaryngoscopePhotoId(entity.getLaryngoscopePhotoId());
        out.setStatus(entity.getPredictionStatus());
        out.setPredictedClassId(entity.getPredictedClassId());
        out.setPredictedLabel(entity.getPredictedLabel());
        out.setConfidence(entity.getConfidence());
        out.setProbabilitiesJson(entity.getProbabilitiesJson());
        out.setHeatmapUrl(entity.getHeatmapUrl());
        out.setModelName(entity.getModelName());
        out.setModelVersion(entity.getModelVersion());
        out.setCreatedTime(entity.getCreationTime() == null ? null : entity.getCreationTime().toString());
        out.setErrorCode(entity.getErrorCode());
        out.setErrorMessage(entity.getErrorMessage());
        out.setProbabilities(parseDoubleList(entity.getProbabilitiesJson()));
        return out;
    }

    public QwenAnalysisOutput toQwenOutput(TLaryngoscopeQwenAnalysisRecord entity) {
        QwenAnalysisOutput out = new QwenAnalysisOutput();
        if (entity == null) return out;
        out.setAnalysisId(entity.getId());
        out.setLaryngoscopePhotoId(entity.getLaryngoscopePhotoId());
        out.setStatus(entity.getAnalysisStatus());
        out.setRiskLevel(entity.getRiskLevel());
        out.setSummaryText(entity.getSummaryText());
        out.setReportJson(entity.getReportJson());
        out.setModelName(entity.getModelName());
        out.setPromptVersion(entity.getPromptVersion());
        out.setIncludeLocalPredictionContext(entity.getIncludeLocalPredictionContext());
        out.setLocalPredictionRecordId(entity.getLocalPredictionRecordId());
        out.setCreatedTime(entity.getCreationTime() == null ? null : entity.getCreationTime().toString());
        out.setErrorCode(entity.getErrorCode());
        out.setErrorMessage(entity.getErrorMessage());
        return out;
    }

    public TLaryngoscopeLocalPredictionRecord findLatestLocal(Integer userId, Integer photoId) {
        return localPredictionMapper.selectOne(
                Wrappers.<TLaryngoscopeLocalPredictionRecord>lambdaQuery()
                        .eq(TLaryngoscopeLocalPredictionRecord::getUserId, userId)
                        .eq(TLaryngoscopeLocalPredictionRecord::getLaryngoscopePhotoId, photoId)
                        .and(w -> w.isNull(TLaryngoscopeLocalPredictionRecord::getIsDelete)
                                .or().eq(TLaryngoscopeLocalPredictionRecord::getIsDelete, false))
                        .orderByDesc(TLaryngoscopeLocalPredictionRecord::getCreationTime)
                        .last("LIMIT 1")
        );
    }

    public TLaryngoscopeQwenAnalysisRecord findLatestQwen(Integer userId, Integer photoId) {
        return qwenAnalysisMapper.selectOne(
                Wrappers.<TLaryngoscopeQwenAnalysisRecord>lambdaQuery()
                        .eq(TLaryngoscopeQwenAnalysisRecord::getUserId, userId)
                        .eq(TLaryngoscopeQwenAnalysisRecord::getLaryngoscopePhotoId, photoId)
                        .and(w -> w.isNull(TLaryngoscopeQwenAnalysisRecord::getIsDelete)
                                .or().eq(TLaryngoscopeQwenAnalysisRecord::getIsDelete, false))
                        .orderByDesc(TLaryngoscopeQwenAnalysisRecord::getCreationTime)
                        .last("LIMIT 1")
        );
    }

    private Integer requireUserId() {
        Integer userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录");
        }
        return userId;
    }

    private TLaryngoscopePhoto requirePhoto(Integer photoId, Integer userId) {
        TLaryngoscopePhoto photo = photoMapper.selectOne(
                Wrappers.<TLaryngoscopePhoto>lambdaQuery()
                        .eq(TLaryngoscopePhoto::getId, photoId)
                        .eq(TLaryngoscopePhoto::getUserId, userId)
                        .and(w -> w.isNull(TLaryngoscopePhoto::getIsDelete)
                                .or().eq(TLaryngoscopePhoto::getIsDelete, false))
                        .last("LIMIT 1")
        );
        if (photo == null) {
            throw new CustomException("喉镜记录不存在或无权限");
        }
        if (!Extension.isNotNullOrEmpty(photo.getLaryngoscopePhotoUrl())) {
            throw new CustomException("喉镜图片为空，无法分析");
        }
        return photo;
    }

    private TLaryngoscopeLocalPredictionRecord createLocalRecord(Integer userId, TLaryngoscopePhoto photo) {
        TLaryngoscopeLocalPredictionRecord record = new TLaryngoscopeLocalPredictionRecord();
        record.setCreationTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setUserId(userId);
        record.setLaryngoscopePhotoId(photo.getId());
        record.setSourcePhotoUrl(photo.getLaryngoscopePhotoUrl());
        record.setPredictionStatus(STATUS_PROCESSING);
        record.setModelName("RepAlexNet");
        record.setModelVersion("repalexnet_heatmap_n100");
        record.setIsDelete(false);
        localPredictionMapper.insert(record);
        return record;
    }

    private TLaryngoscopeQwenAnalysisRecord createQwenRecord(Integer userId, TLaryngoscopePhoto photo, boolean includeLocal) {
        TLaryngoscopeQwenAnalysisRecord record = new TLaryngoscopeQwenAnalysisRecord();
        record.setCreationTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setUserId(userId);
        record.setLaryngoscopePhotoId(photo.getId());
        record.setSourcePhotoUrl(photo.getLaryngoscopePhotoUrl());
        record.setAnalysisStatus(STATUS_PROCESSING);
        record.setReportType(QWEN_REPORT_TYPE);
        record.setModelProvider(PROVIDER);
        record.setModelName(QWEN_MODEL_NAME);
        record.setPromptVersion(QWEN_PROMPT_VERSION);
        record.setIncludeLocalPredictionContext(includeLocal);
        record.setIsDelete(false);
        qwenAnalysisMapper.insert(record);
        return record;
    }

    private LocalPredictionOutput failLocal(TLaryngoscopeLocalPredictionRecord record, String code, String message, long start) {
        int durationMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        record.setUpdateTime(LocalDateTime.now());
        record.setPredictionStatus(STATUS_FAILED);
        record.setErrorCode(code);
        record.setErrorMessage(message);
        record.setDurationMs(durationMs);
        localPredictionMapper.updateById(record);
        return toLocalOutput(record);
    }

    private QwenAnalysisOutput failQwen(TLaryngoscopeQwenAnalysisRecord record, String code, String message, long start) {
        int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        record.setUpdateTime(LocalDateTime.now());
        record.setAnalysisStatus(STATUS_FAILED);
        record.setParseStatus(STATUS_FAILED);
        record.setErrorCode(code);
        record.setErrorMessage(message);
        record.setLatencyMs(latencyMs);
        qwenAnalysisMapper.updateById(record);
        return toQwenOutput(record);
    }

    private TLaryngoscopeLocalPredictionRecord findLatestLocalSuccess(Integer userId, Integer photoId) {
        return localPredictionMapper.selectOne(
                Wrappers.<TLaryngoscopeLocalPredictionRecord>lambdaQuery()
                        .eq(TLaryngoscopeLocalPredictionRecord::getUserId, userId)
                        .eq(TLaryngoscopeLocalPredictionRecord::getLaryngoscopePhotoId, photoId)
                        .eq(TLaryngoscopeLocalPredictionRecord::getPredictionStatus, STATUS_SUCCESS)
                        .and(w -> w.isNull(TLaryngoscopeLocalPredictionRecord::getIsDelete)
                                .or().eq(TLaryngoscopeLocalPredictionRecord::getIsDelete, false))
                        .orderByDesc(TLaryngoscopeLocalPredictionRecord::getCreationTime)
                        .last("LIMIT 1")
        );
    }

    private TLaryngoscopeQwenAnalysisRecord findLatestQwenSuccess(Integer userId, Integer photoId, boolean includeLocal) {
        return qwenAnalysisMapper.selectOne(
                Wrappers.<TLaryngoscopeQwenAnalysisRecord>lambdaQuery()
                        .eq(TLaryngoscopeQwenAnalysisRecord::getUserId, userId)
                        .eq(TLaryngoscopeQwenAnalysisRecord::getLaryngoscopePhotoId, photoId)
                        .eq(TLaryngoscopeQwenAnalysisRecord::getAnalysisStatus, STATUS_SUCCESS)
                        .eq(TLaryngoscopeQwenAnalysisRecord::getIncludeLocalPredictionContext, includeLocal)
                        .and(w -> w.isNull(TLaryngoscopeQwenAnalysisRecord::getIsDelete)
                                .or().eq(TLaryngoscopeQwenAnalysisRecord::getIsDelete, false))
                        .orderByDesc(TLaryngoscopeQwenAnalysisRecord::getCreationTime)
                        .last("LIMIT 1")
        );
    }

    private File resolveLocalFileFromUrl(String url) throws Exception {
        String normalized = url == null ? "" : url.trim();
        if (normalized.isEmpty()) {
            throw new LaryngoscopeFailure("LOCAL_IMAGE_FILE_MISSING", "图片URL为空");
        }

        File direct = new File(normalized);
        if (direct.exists()) {
            return direct;
        }

        String path = normalized;
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            path = new URI(normalized).getPath();
        }
        path = URLDecoder.decode(path, StandardCharsets.UTF_8);
        path = path.replace("\\", "/");
        String[] parts = path.split("/");
        if (parts.length < 2) {
            throw new LaryngoscopeFailure("LOCAL_IMAGE_FILE_MISSING", "图片URL格式不正确：" + url);
        }
        String filename = parts[parts.length - 1];
        String folder = parts[parts.length - 2];
        File userDir = new File(System.getProperty("user.dir"));
        File candidate = new File(new File(new File(userDir, "external-resources"), folder), filename);
        if (candidate.exists()) return candidate;

        File parent = userDir.getParentFile();
        if (parent != null) {
            File parentCandidate = new File(new File(new File(parent, "external-resources"), folder), filename);
            if (parentCandidate.exists()) return parentCandidate;
        }

        throw new LaryngoscopeFailure("LOCAL_IMAGE_FILE_MISSING", "找不到喉镜图片文件：" + candidate.getAbsolutePath());
    }

    private File resolveRepoRoot() {
        File userDir = new File(System.getProperty("user.dir"));
        if (new File(userDir, "laryngoscope prediction").exists()) return userDir;
        File parent = userDir.getParentFile();
        if (parent != null && new File(parent, "laryngoscope prediction").exists()) return parent;
        return userDir;
    }

    private File resolveFile(String configuredPath, File repoRoot) {
        File f = new File(configuredPath);
        if (f.isAbsolute()) return f;
        return new File(repoRoot, configuredPath);
    }

    private File resolveModelFile(File repoRoot) {
        File configured = resolveFile(modelPath, repoRoot);
        if (configured.exists()) return configured;
        File nested = new File(repoRoot, "laryngoscope prediction/store/repalexnet_heatmap_n100.pth");
        if (nested.exists()) return nested;
        return configured;
    }

    private File buildPredictionRunDir(Integer photoId) {
        File userDir = new File(System.getProperty("user.dir"));
        File root = new File(outputRoot);
        if (!root.isAbsolute()) {
            root = new File(userDir, outputRoot);
        }
        return new File(root, photoId + "_" + System.currentTimeMillis());
    }

    private void runPredictionProcess(File sourceFile, File scriptFile, File modelFile, File runDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                "-u",
                scriptFile.getAbsolutePath(),
                "--input",
                sourceFile.getAbsolutePath(),
                "--model-path",
                modelFile.getAbsolutePath(),
                "--output-dir",
                runDir.getAbsolutePath(),
                "--device",
                Extension.isNotNullOrEmpty(device) ? device : "auto"
        );
        pb.directory(scriptFile.getParentFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PYTHONIOENCODING", "utf-8");

        Process process = pb.start();
        int timeout = timeoutSeconds == null || timeoutSeconds <= 0 ? 120 : timeoutSeconds;
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new LaryngoscopeFailure("LOCAL_MODEL_TIMEOUT", "本地模型预测超时（>" + timeout + "秒）");
        }
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        }
        if (process.exitValue() != 0) {
            throw new LaryngoscopeFailure("LOCAL_MODEL_PROCESS_FAILED", output.toString().trim());
        }
    }

    private JsonNode readPredictionPayload(File reportFile) throws Exception {
        if (!reportFile.exists()) {
            throw new LaryngoscopeFailure("LOCAL_MODEL_OUTPUT_INVALID", "预测结果文件不存在：" + reportFile.getAbsolutePath());
        }
        return mapper.readTree(reportFile);
    }

    private File resolveHeatmapFile(String heatmapPath, File runDir, File repoRoot) {
        if (!Extension.isNotNullOrEmpty(heatmapPath)) {
            return new File(runDir, "heatmaps");
        }
        File raw = new File(heatmapPath);
        if (raw.isAbsolute()) return raw;
        File inRun = new File(runDir, heatmapPath);
        if (inRun.exists()) return inRun;
        File inScriptRoot = new File(repoRoot, "laryngoscope prediction/" + heatmapPath);
        if (inScriptRoot.exists()) return inScriptRoot;
        return inRun;
    }

    private String toExternalResourceUrl(File file) {
        try {
            File userDir = new File(System.getProperty("user.dir"));
            Path externalRoot = new File(userDir, "external-resources").toPath().toAbsolutePath().normalize();
            Path target = file.toPath().toAbsolutePath().normalize();
            if (!target.startsWith(externalRoot)) {
                File parent = userDir.getParentFile();
                if (parent != null) {
                    externalRoot = new File(parent, "external-resources").toPath().toAbsolutePath().normalize();
                }
            }
            String rel = externalRoot.relativize(target).toString().replace("\\", "/");
            return ServletUriComponentsBuilder.fromCurrentContextPath().path("/").path(rel).toUriString();
        } catch (Exception ex) {
            return file.toURI().toString();
        }
    }

    private String buildLocalPredictionSnapshot(TLaryngoscopeLocalPredictionRecord local) throws Exception {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("PredictionId", local.getId());
        snapshot.put("PredictedClassId", local.getPredictedClassId());
        snapshot.put("PredictedLabel", local.getPredictedLabel());
        snapshot.put("Confidence", local.getConfidence());
        snapshot.put("ModelName", local.getModelName());
        snapshot.put("ModelVersion", local.getModelVersion());
        return mapper.writeValueAsString(snapshot);
    }

    private String resolveImageMimeType(File sourceFile) {
        try {
            String mimeType = Files.probeContentType(sourceFile.toPath());
            if (Extension.isNotNullOrEmpty(mimeType) && mimeType.startsWith("image/")) {
                return mimeType;
            }
        } catch (Exception ignore) {
        }
        String name = sourceFile.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".bmp")) return "image/bmp";
        return "image/png";
    }

    private String buildLaryngoscopePrompt(TLaryngoscopePhoto photo, boolean includeLocal, String localSnapshot) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
你将收到一张用户上传的喉镜图片。请基于图片中可见的喉部影像信息，给出健康风险提示和就医建议。

重要约束：
1. 你不是医生，不能做出医疗确诊，也不能替代耳鼻喉科医生的面诊、喉镜检查、病理检查或其他临床判断。
2. 不要使用“确诊”“必然”“一定是”“已经排除”“良性/恶性定论”等措辞。
3. 对疑似异常只能表述为“可能提示”“建议进一步检查”“建议结合症状和医生评估”。
4. 如果图片模糊、遮挡、角度不足、光线不佳或信息不足，请把 risk_level 设为“无法判断”，并说明需要重新拍摄或线下就诊。
5. 输出必须是 JSON，不要输出 Markdown、代码块或额外解释。

JSON 结构必须如下：
{
  "risk_level": "低|中|高|无法判断",
  "plain_summary": "string",
  "image_observations": ["string"],
  "possible_explanations": ["string"],
  "suggestions": ["string"],
  "when_to_see_doctor": ["string"],
  "retest_tips": ["string"],
  "disclaimer": "本结果仅供健康参考，不替代医生诊断。"
}
""");
        prompt.append("\n补充上下文（由用户填写，供参考）：\n");
        appendContext(prompt, "检查时间", photo.getCheckTime() == null ? null : photo.getCheckTime().toString());
        appendContext(prompt, "检查医院/科室", photo.getHospitalName());
        appendContext(prompt, "检查方式", photo.getCheckType());
        appendContext(prompt, "视角/体位", photo.getViewPosition());
        appendContext(prompt, "病变侧别", photo.getLesionSide());
        appendContext(prompt, "备注", photo.getPhotoDesc());
        if (includeLocal && Extension.isNotNullOrEmpty(localSnapshot)) {
            prompt.append("\n本地 RepAlexNet 模型预测结果（仅作为参考信息，不可作为诊断结论）：\n");
            prompt.append(localSnapshot).append("\n");
        }
        return prompt.toString();
    }

    private void appendContext(StringBuilder prompt, String label, String value) {
        if (Extension.isNotNullOrEmpty(value)) {
            prompt.append("- ").append(label).append("：").append(value).append("\n");
        }
    }

    private QwenParse parseQwenReport(String modelText) {
        String raw = modelText == null ? "" : modelText.trim();
        if (raw.isEmpty()) {
            return new QwenParse(STATUS_FAILED, null, null, null, "模型返回为空");
        }
        if (raw.startsWith("```")) {
            raw = raw.replaceFirst("^```[a-zA-Z]*", "").replaceFirst("```$", "").trim();
        }
        int first = raw.indexOf('{');
        int last = raw.lastIndexOf('}');
        if (first >= 0 && last > first) {
            raw = raw.substring(first, last + 1);
        }
        try {
            JsonNode node = mapper.readTree(raw);
            String riskLevel = node.path("risk_level").asText("无法判断");
            String summary = node.path("plain_summary").asText("");
            if (!Extension.isNotNullOrEmpty(summary)) {
                summary = "Qwen 图像分析完成，请查看结构化建议。";
            }
            return new QwenParse(STATUS_SUCCESS, raw, riskLevel, summary, null);
        } catch (Exception ex) {
            return new QwenParse(STATUS_FAILED, null, null, "Qwen 图像分析返回内容无法解析", ex.getMessage());
        }
    }

    private List<Double> parseDoubleList(String json) {
        if (!Extension.isNotNullOrEmpty(json)) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(json, new TypeReference<List<Double>>() {});
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private static class LaryngoscopeFailure extends Exception {
        private final String code;

        private LaryngoscopeFailure(String code, String message) {
            super(message);
            this.code = code;
        }
    }

    private static class QwenParse {
        private final String parseStatus;
        private final String reportJson;
        private final String riskLevel;
        private final String summaryText;
        private final String errorMessage;

        private QwenParse(String parseStatus, String reportJson, String riskLevel, String summaryText, String errorMessage) {
            this.parseStatus = parseStatus;
            this.reportJson = reportJson;
            this.riskLevel = riskLevel;
            this.summaryText = summaryText;
            this.errorMessage = errorMessage;
        }
    }
}
