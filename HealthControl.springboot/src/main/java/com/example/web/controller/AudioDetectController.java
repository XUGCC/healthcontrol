package com.example.web.controller;

import com.example.web.dto.TAudioScreenRecordDto;
import com.example.web.entity.TModelInterfaceCallLog;
import com.example.web.mapper.TModelInterfaceCallLogMapper;
import com.example.web.service.TAudioScreenRecordService;
import com.example.web.service.impl.TPersonalLaryngealHealthRecordServiceImpl;
import com.example.web.tools.BaseContext;
import com.example.web.tools.dto.CurrentUserDto;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 核心音频检测接口（同步调用 Python，直接返回结果或错误 JSON）
 *
 * 对应需求文档中的：
 *  - POST /api/audio/detect
 *  - 小程序通过 uni.uploadFile 上传音频，后端保存临时文件并通过 ProcessBuilder 调用 Python 脚本
 *  - Python 统一输出一条 JSON，后端按 success/data/error 封装返回
 */
@RestController
@RequestMapping("/api/audio")
public class AudioDetectController {

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTS = new String[] { "mp3", "wav", "flac", "m4a" };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TAudioScreenRecordService audioScreenRecordService;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TModelInterfaceCallLogMapper modelInterfaceCallLogMapper;

    public AudioDetectController(TAudioScreenRecordService audioScreenRecordService) {
        this.audioScreenRecordService = audioScreenRecordService;
    }

    /**
     * 上传并检测音频（同步）
     *
     * Response 结构（直接返回，不再套一层 ResponseData）：
     * {
     *   "success": true/false,
     *   "data": {...},   // success=true 时为 Python 返回 JSON
     *   "error": {...},  // success=false 时为 Python 或 Java 聚合的错误 JSON
     *   "requestId": "uuid",
     *   "message": "检测成功/检测失败/参数错误/系统异常"
     * }
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadAndDetect(
            @RequestPart("file") MultipartFile file,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        String requestId = UUID.randomUUID().toString();

        // 优先从 token 中解析当前登录用户，再兜底使用请求参数 userId（为兼容旧前端）
        Integer userId = null;
        CurrentUserDto currentUser = BaseContext.getCurrentUserDto();
        if (currentUser != null && currentUser.getUserId() != null && currentUser.getUserId() > 0) {
            userId = currentUser.getUserId();
        } else {
            try {
                String userIdStr = request.getParameter("userId");
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    userId = Integer.valueOf(userIdStr);
                }
            } catch (Exception ignore) {
                // 参数解析失败时忽略，最多导致本次不写入历史记录
            }
        }

        // ---------- 0. 基础校验：文件是否存在 ----------
        if (file == null || file.isEmpty()) {
            return buildOuterError(
                    requestId,
                    "上传的音频文件为空",
                    "AUDIO_INVALID",
                    "upload_validation",
                    null,
                    null
            );
        }

        String originalName = file.getOriginalFilename();
        String ext = (originalName == null) ? "" : StringUtils.getFilenameExtension(originalName);
        ext = (ext == null) ? "" : ext.toLowerCase();

        // ---------- 1. 校验扩展名 ----------
        boolean allowed = false;
        for (String e : ALLOWED_EXTS) {
            if (e.equals(ext)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            return buildOuterError(
                    requestId,
                    "不支持的音频格式，仅支持 mp3 / wav / flac / m4a",
                    "AUDIO_INVALID_FORMAT",
                    "upload_validation",
                    null,
                    null
            );
        }

        // ---------- 2. 校验大小 ----------
        if (file.getSize() > MAX_SIZE_BYTES) {
            return buildOuterError(
                    requestId,
                    "音频文件过大，最大支持 10MB",
                    "AUDIO_TOO_LARGE",
                    "upload_validation",
                    null,
                    null
            );
        }

        // ---------- 3. 将文件保存到本地临时目录 ----------
        long preprocessStartNanos = System.nanoTime();
        File tempDir = buildTempDir();
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            return buildOuterError(
                    requestId,
                    "服务器创建临时目录失败",
                    "SERVER_IO_ERROR",
                    "resolve_paths",
                    null,
                    null
            );
        }

        String safeName = buildSafeFileName(originalName, ext);
        File audioFile = new File(tempDir, safeName);
        try {
            file.transferTo(audioFile);
        } catch (Exception e) {
            return buildOuterError(
                    requestId,
                    "保存上传的音频文件失败：" + e.getMessage(),
                    "SERVER_IO_ERROR",
                    "resolve_paths",
                    null,
                    getStackTrace(e)
            );
        }

        // 所在静态资源目录的相对文件夹名（用于组装图谱 URL）
        final String folderName = tempDir.getName();

        // ---------- 4. 调用 Python 脚本 ----------
        try {
            String userDir = System.getProperty("user.dir");
            File scriptFile = resolvePythonScript(userDir);
            if (scriptFile == null || !scriptFile.exists()) {
                return buildOuterError(
                        requestId,
                        "服务器缺少检测脚本 detect_audio.py，请联系管理员部署模型服务",
                        "PYTHON_SCRIPT_MISSING",
                        "resolve_paths",
                        null,
                        null
                );
            }

            long pythonStartNanos = System.nanoTime();

            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "-u",
                    scriptFile.getAbsolutePath(),
                    "--audio",
                    audioFile.getAbsolutePath(),
                    "--outdir",
                    tempDir.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            pb.environment().put("PYTHONIOENCODING", "utf-8");

            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            // 等待脚本完成，最长 120 秒
            boolean finished = p.waitFor(120, TimeUnit.SECONDS);
            long pythonEndNanos = System.nanoTime();
            int pythonCostMs = (int) TimeUnit.NANOSECONDS.toMillis(pythonEndNanos - pythonStartNanos);
            if (!finished) {
                p.destroyForcibly();
                return buildOuterError(
                        requestId,
                        "检测脚本执行超时（>120 秒），请稍后重试或联系管理员",
                        "PYTHON_TIMEOUT",
                        "inference",
                        output.toString(),
                        null
                );
            }

            String raw = output.toString().trim();
            if (raw.isEmpty()) {
                return buildOuterError(
                        requestId,
                        "检测脚本无输出，请联系管理员检查 Python 环境",
                        "PYTHON_NO_OUTPUT",
                        "java_parse_python_json",
                        raw,
                        null
                );
            }

            int lastL = raw.lastIndexOf('{');
            int lastR = raw.lastIndexOf('}');
            if (lastL < 0 || lastR < 0 || lastR <= lastL) {
                return buildOuterError(
                        requestId,
                        "检测脚本输出无法解析为 JSON，请联系管理员检查脚本实现",
                        "JAVA_PARSE_JSON_FAIL",
                        "java_parse_python_json",
                        raw,
                        null
                );
            }
            String json = raw.substring(lastL, lastR + 1);

            JsonNode node = objectMapper.readTree(json);
            boolean pySuccess = node.path("success").asBoolean(false);

            Map<String, Object> inner = objectMapper.convertValue(
                    node,
                    new TypeReference<Map<String, Object>>() {
                    }
            );

            // 计算当前服务对外基础地址（例如 http://localhost:7245）
            String scheme = request.getScheme();
            String host = request.getServerName();
            int port = request.getServerPort();
            String baseUrl = scheme + "://" + host + ":" + port;

            // 组装 MFCC / Mel 图谱的可访问 URL（基于 external-resources 静态映射）
            Object mfccFileObj = inner.get("mfcc_file");
            Object melFileObj = inner.get("mel_file");
            Object waveformFileObj = inner.get("waveform_file");
            if (mfccFileObj instanceof String mfccName && !mfccName.isEmpty()) {
                inner.put("mfccUrl", baseUrl + "/" + folderName + "/" + mfccName);
            }
            if (melFileObj instanceof String melName && !melName.isEmpty()) {
                inner.put("melUrl", baseUrl + "/" + folderName + "/" + melName);
            }
            if (waveformFileObj instanceof String waveName && !waveName.isEmpty()) {
                inner.put("waveformUrl", baseUrl + "/" + folderName + "/" + waveName);
            }

            // ---------- 5. 将检测结果写入音频自查历史记录表 ----------
            // 只要检测成功就尝试写库；userId 为空时仍允许写入（此时该记录只能在管理端查看）
            Integer savedDetectId = null;
            if (pySuccess) {
                try {
                    TAudioScreenRecordDto dto = new TAudioScreenRecordDto();
                    dto.setUserId(userId);
                    dto.setUpdateTime(LocalDateTime.now());

                    // 音频与图谱 URL
                    String audioUrl = baseUrl + "/" + folderName + "/" + safeName;
                    dto.setAudioUrl(audioUrl);
                    dto.setAudioFormat(ext);
                    // 尽量补充一些基础指标，避免数据库非空约束
                    dto.setAudioFileSize((int) audioFile.length());
                    dto.setPronunciationGuideType(false);
                    dto.setWaveformUrl((String) inner.get("waveformUrl"));
                    dto.setMfccSpectrumUrl((String) inner.get("mfccUrl"));
                    dto.setMelSpectrumUrl((String) inner.get("melUrl"));

                    // 语音性别相关（来自 Python Siamese 多任务模型）
                    Object genderLabelObj = inner.get("voiceGender");
                    if (genderLabelObj instanceof String) {
                        dto.setVoiceGender((String) genderLabelObj);
                    }
                    Object genderCodeObj = inner.get("voiceGenderCode");
                    if (genderCodeObj instanceof Number) {
                        dto.setVoiceGenderCode(((Number) genderCodeObj).intValue());
                    }
                    Object genderConfObj = inner.get("voiceGenderConfidence");
                    if (genderConfObj instanceof Number) {
                        dto.setVoiceGenderConfidence(((Number) genderConfObj).doubleValue());
                    }

                    // 模型与结果信息
                    boolean malignant = node.path("malignant").asBoolean(false);
                    double confidence = node.path("confidence").asDouble(0.0);
                    dto.setPrimaryScreenResult(malignant);
                    dto.setPrimaryScreenConfidence(confidence);
                    // 为避免数据库字段长度限制导致 Data too long，这里暂不写入模型版本，由后续需要时再扩字段
                    dto.setDensenetModelVersion(null);

                    dto.setDetectTotalStatus(true);
                    dto.setDetectSubStatus(true);
                    dto.setIsDelete(false);
                    dto.setOfflineStatus(false);

                    TAudioScreenRecordDto savedDto = audioScreenRecordService.CreateOrEdit(dto);
                    if (savedDto != null && savedDto.getId() != null) {
                        savedDetectId = savedDto.getId();
                    }
                    
                    // 检测成功后，自动更新健康档案
                    if (userId != null && userId > 0 && savedDetectId != null) {
                        try {
                            TPersonalLaryngealHealthRecordServiceImpl healthRecordService = 
                                applicationContext.getBean(TPersonalLaryngealHealthRecordServiceImpl.class);
                            healthRecordService.AutoUpdateAfterDetect(userId, savedDetectId);
                        } catch (Exception e) {
                            // 档案更新失败不影响检测结果返回
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    // 历史记录写入失败不影响本次检测结果返回，但需要在服务器日志中可见
                    e.printStackTrace();
                }
            }

            // ---------- 6. 写入模型接口调用日志（TModelInterfaceCallLog），一条预处理 + 一条推理 ----------
            try {
                if (savedDetectId != null) {
                    LocalDateTime now = LocalDateTime.now();

                    // 公共：模型版本，从 Python 结果中取 method
                    String modelVersion = null;
                    if (inner.get("method") instanceof String) {
                        modelVersion = (String) inner.get("method");
                    }

                    // 6.1 预处理环节（CallLink=0）
                    int preprocessCostMs = (int) TimeUnit.NANOSECONDS.toMillis(pythonStartNanos - preprocessStartNanos);
                    TModelInterfaceCallLog pre = new TModelInterfaceCallLog();
                    pre.setDetectId(savedDetectId);
                    pre.setUpdateTime(now);
                    pre.setCallLink(Boolean.FALSE); // 0 = 预处理
                    pre.setModelInterfaceType(Boolean.TRUE); // 在线实时检测
                    pre.setModelVersion(modelVersion);
                    pre.setServiceName("audio_preprocess");
                    pre.setInputSpectrumUrl(null);
                    pre.setInputParams("audio_preprocess");
                    pre.setCallCost(preprocessCostMs);
                    pre.setQualityLevel(evaluateQuality(preprocessCostMs));
                    pre.setResultSummary("预处理完成");
                    pre.setResultComplete(null);
                    pre.setCallStatus(true);
                    pre.setFailErrorCode(null);
                    pre.setFailDetailInfo(null);
                    pre.setIsDelete(false);
                    modelInterfaceCallLogMapper.insert(pre);

                    // 6.2 模型推理环节（CallLink=1）
                    TModelInterfaceCallLog infer = new TModelInterfaceCallLog();
                    infer.setDetectId(savedDetectId);
                    infer.setUpdateTime(now);
                    infer.setCallLink(Boolean.TRUE); // 1 = 特征提取/模型推断
                    infer.setModelInterfaceType(Boolean.TRUE);
                    infer.setModelVersion(modelVersion);
                    infer.setServiceName("audio_siamese_inference");
                    infer.setInputSpectrumUrl(null);
                    infer.setInputParams("audio_detect");
                    infer.setCallCost(pythonCostMs);
                    infer.setQualityLevel(evaluateQuality(pythonCostMs));
                    infer.setResultSummary(pySuccess ? "检测成功" : "检测失败");
                    infer.setResultComplete(json);
                    infer.setCallStatus(pySuccess);
                    if (!pySuccess) {
                        String errorCode = node.path("errorCode").asText("");
                        String stage = node.path("stage").asText("");
                        String errorMessage = node.path("errorMessage").asText("");
                        infer.setFailErrorCode(errorCode);
                        infer.setFailDetailInfo(stage + ":" + errorMessage);
                    }
                    infer.setIsDelete(false);
                    modelInterfaceCallLogMapper.insert(infer);

                    // 6.3 结果写入/报告生成环节（不区分 CallLink，用 ServiceName 区分）
                    TModelInterfaceCallLog persist = new TModelInterfaceCallLog();
                    persist.setDetectId(savedDetectId);
                    persist.setUpdateTime(now);
                    persist.setCallLink(null);
                    persist.setModelInterfaceType(Boolean.TRUE);
                    persist.setModelVersion(modelVersion);
                    persist.setServiceName("audio_result_persist");
                    persist.setInputSpectrumUrl(null);
                    persist.setInputParams("result_persist");
                    // 结果写入通常很快，这里简单按推理耗时的 5% 估算，避免 0
                    int persistCostMs = Math.max(1, pythonCostMs / 20);
                    persist.setCallCost(persistCostMs);
                    persist.setQualityLevel(evaluateQuality(persistCostMs));
                    persist.setResultSummary("结果入库/健康档案更新");
                    persist.setResultComplete(null);
                    persist.setCallStatus(true);
                    persist.setFailErrorCode(null);
                    persist.setFailDetailInfo(null);
                    persist.setIsDelete(false);
                    modelInterfaceCallLogMapper.insert(persist);
                }
            } catch (Exception e) {
                // 日志写入失败不能影响主流程
                e.printStackTrace();
            }

            Map<String, Object> outer = new LinkedHashMap<>();
            outer.put("success", pySuccess);
            outer.put("requestId", requestId);
            outer.put("message", pySuccess ? "检测成功" : "检测失败");
            if (pySuccess) {
                // 成功：直接将 Python 的 JSON 作为 data 透传给前端
                // 同时将 Java 侧写入的检测记录 Id 透传给前端，便于前端关联模型调用日志等信息
                if (savedDetectId != null) {
                    inner.put("Id", savedDetectId);
                    inner.put("detectId", savedDetectId);
                }
                outer.put("data", inner);
            } else {
                // 失败：将 Python 的 JSON 作为 error 透传给前端
                outer.put("error", inner);
            }
            return outer;
        } catch (Exception e) {
            return buildOuterError(
                    requestId,
                    "服务器内部异常：" + e.getMessage(),
                    "UNKNOWN",
                    "java_exception",
                    null,
                    getStackTrace(e)
            );
        }
    }

    private File buildTempDir() {
        String userDir = System.getProperty("user.dir");
        String folder = "upload-audio-" + Instant.now().toEpochMilli();
        return new File(userDir + File.separator + "external-resources", folder);
    }

    private String buildSafeFileName(String originalName, String ext) {
        String base = (originalName == null ? "audio" : originalName);
        base = base.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        if (!StringUtils.hasText(ext)) {
            return base + "_" + UUID.randomUUID() + ".mp3";
        }
        return base + "_" + UUID.randomUUID() + "." + ext;
    }

    /**
     * 根据耗时粗略评估服务质量等级：0=正常,1=轻微异常,2=严重异常
     */
    private int evaluateQuality(int costMs) {
        if (costMs <= 10000) {
            return 0;
        }
        if (costMs <= 30000) {
            return 1;
        }
        return 2;
    }

    private File resolvePythonScript(String userDir) {
        // 仅查找新的 Siamese 多任务脚本 detect_audio_siamese.py
        // 情况1：工作目录在项目根目录
        File primary = new File(userDir + File.separator + "voice" + File.separator + "detect_audio_siamese.py");
        if (primary.exists()) {
            return primary;
        }
        // 情况2：工作目录在 HealthControl.springboot，下一级为后端工程，上一级为项目根
        File alt = new File(
                new File(userDir).getParent() + File.separator + "voice" + File.separator + "detect_audio_siamese.py");
        if (alt.exists()) {
            return alt;
        }
        // 不再回退到旧的 detect_audio.py，让上层显式提示“脚本缺失”，便于发现部署问题
        return primary;
    }

    private Map<String, Object> buildOuterError(
            String requestId,
            String message,
            String errorCode,
            String stage,
            String rawOutput,
            String stackTrace
    ) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("stage", stage);
        error.put("errorType", errorCode);
        error.put("errorMessage", message);
        error.put("errorCode", errorCode);
        if (rawOutput != null) {
            error.put("rawOutput", rawOutput);
        }
        if (stackTrace != null) {
            error.put("stackTrace", stackTrace);
        }

        Map<String, Object> outer = new LinkedHashMap<>();
        outer.put("success", false);
        outer.put("error", error);
        outer.put("requestId", requestId);
        outer.put("message", message);
        return outer;
    }

    private String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append(t.toString()).append("\n");
        for (StackTraceElement e : t.getStackTrace()) {
            sb.append("    at ").append(e.toString()).append("\n");
        }
        return sb.toString();
    }
}

