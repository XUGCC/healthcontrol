package com.example.web.service.front.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.web.dto.TAudioAiReportDto;
import com.example.web.dto.TAudioScreenRecordDto;
import com.example.web.dto.front.FrontAIDtos;
import com.example.web.dto.front.FrontAIDtos.FrontAIAnalyzeAudioRecordInput;
import com.example.web.dto.front.FrontAIDtos.FrontAIChatInput;
import com.example.web.dto.front.FrontAIDtos.FrontAIChatOutput;
import com.example.web.dto.front.FrontAIDtos.FrontAIReportOutput;
import com.example.web.dto.query.TAudioScreenRecordPagedInput;
import com.example.web.entity.TAudioAiReport;
import com.example.web.entity.TModelInterfaceCallLog;
import com.example.web.mapper.TAudioAiReportMapper;
import com.example.web.mapper.TModelInterfaceCallLogMapper;
import com.example.web.service.TAudioAiReportService;
import com.example.web.service.TAudioScreenRecordService;
import com.example.web.service.front.FrontAIService;
import com.example.web.tools.BaseContext;
import com.example.web.tools.Extension;
import com.example.web.tools.exception.CustomException;
import com.example.web.tools.ai.QwenBailianClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class FrontAIServiceImpl implements FrontAIService {

    private static final String PROVIDER = "DashScope";
    private static final String REPORT_TYPE_RECORD = "RECORD_INTERPRETATION";

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private QwenBailianClient qwenClient;

    @Autowired
    private TAudioScreenRecordService audioScreenRecordService;

    @Autowired
    private TAudioAiReportService audioAiReportService;

    @Autowired
    private TAudioAiReportMapper audioAiReportMapper;

    @Autowired
    private TModelInterfaceCallLogMapper modelInterfaceCallLogMapper;

    @Override
    public FrontAIReportOutput analyzeAudioRecord(FrontAIAnalyzeAudioRecordInput input) {
        if (input == null || input.getRecordId() == null || input.getRecordId() <= 0) {
            throw new CustomException("缺少RecordId");
        }
        Integer userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录");
        }

        String spectrumType = normalizeSpectrumType(input.getSpectrumType());
        boolean force = Boolean.TRUE.equals(input.getForceRegenerate());

        // 1) 若已有最新成功报告且不强制重跑，则直接返回
        if (!force) {
            TAudioAiReport latest = findLatestSuccessReport(userId, input.getRecordId(), REPORT_TYPE_RECORD, spectrumType);
            if (latest != null) {
                return toOutput(latest);
            }
        }

        // 2) 读取检测记录，校验归属（通过 service Get）
        TAudioScreenRecordPagedInput getInput = new TAudioScreenRecordPagedInput();
        getInput.setId(input.getRecordId());
        getInput.setUserId(userId);
        getInput.setPage(1L);
        getInput.setLimit(1L);
        TAudioScreenRecordDto recordDto = audioScreenRecordService.Get(getInput);
        if (recordDto == null || recordDto.getId() == null) {
            throw new CustomException("检测记录不存在或无权限");
        }
        if (recordDto.getDetectTotalStatus() == null || recordDto.getDetectTotalStatus() == false) {
            throw new CustomException("该记录检测尚未完成，无法生成AI解读");
        }

        // 3) 准备图谱 PNG（Base64）
        byte[] pngBytes = loadSpectrumPngBytes(recordDto, spectrumType);
        String pngBase64 = Base64.getEncoder().encodeToString(pngBytes);

        // 4) 构造 prompt：包含“非医疗诊断”约束 + JSON schema
        String prompt = buildSpectrumPrompt(recordDto, spectrumType);

        // 5) 调用 Qwen-VL
        long start = System.nanoTime();
        QwenBailianClient.QwenResponse resp;
        try {
            resp = qwenClient.chatWithPngBase64(prompt, pngBase64);
        } catch (Exception ex) {
            int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            TAudioAiReport fail = persistReportFail(userId, input.getRecordId(), REPORT_TYPE_RECORD, spectrumType, latencyMs,
                    "DASHSCOPE_CALL_FAILED", ex.getMessage(), prompt, null);
            writeCallLog(recordDto.getId(), spectrumType, false, latencyMs, "qwen_vl_call_failed", ex.getMessage());
            return toOutput(fail);
        }
        int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // 6) 解析输出为 JSON（容错）
        ParseResult parse = parseReportJson(resp.contentText());

        // 7) 落库报告 + 写日志
        TAudioAiReport saved = persistReportSuccess(userId, input.getRecordId(), REPORT_TYPE_RECORD, spectrumType, latencyMs,
                parse, prompt, resp.rawResponse());
        writeCallLog(recordDto.getId(), spectrumType, true, latencyMs, "qwen_vl_call_ok", null);

        return toOutput(saved);
    }

    @Override
    public FrontAIReportOutput analyzeSpectrumUpload(String spectrumType, MultipartFile file) {
        Integer userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录");
        }
        if (file == null || file.isEmpty()) {
            throw new CustomException("请上传图谱PNG文件");
        }
        String st = normalizeSpectrumType(spectrumType);
        if (!"MEL".equals(st) && !"MFCC".equals(st)) {
            throw new CustomException("SpectrumType 仅支持 MEL 或 MFCC");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception e) {
            throw new CustomException("读取上传文件失败：" + e.getMessage());
        }
        String pngBase64 = Base64.getEncoder().encodeToString(bytes);

        String prompt = buildUploadSpectrumPrompt(st);
        long start = System.nanoTime();
        QwenBailianClient.QwenResponse resp;
        try {
            resp = qwenClient.chatWithPngBase64(prompt, pngBase64);
        } catch (Exception ex) {
            int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            TAudioAiReport fail = persistReportFail(userId, null, "SPECTRUM_UPLOAD", st, latencyMs,
                    "DASHSCOPE_CALL_FAILED", ex.getMessage(), prompt, null);
            return toOutput(fail);
        }
        int latencyMs = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        ParseResult parse = parseReportJson(resp.contentText());
        TAudioAiReport saved = persistReportSuccess(userId, null, "SPECTRUM_UPLOAD", st, latencyMs, parse, prompt, resp.rawResponse());
        return toOutput(saved);
    }

    @Override
    public FrontAIChatOutput chat(FrontAIChatInput input) {
        Integer userId = BaseContext.getCurrentUserId();
        if (userId == null || userId <= 0) {
            throw new CustomException("请先登录");
        }
        if (input == null || !Extension.isNotNullOrEmpty(input.getQuestion())) {
            throw new CustomException("请输入咨询问题");
        }

        // 1) 组装 messages（系统约束 + 历史 + 当前问题）
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildMedicalChatSystemPrompt()));

        if (input.getHistory() != null) {
            for (FrontAIDtos.FrontAIChatMessage m : input.getHistory()) {
                if (m == null) continue;
                String role = (m.getRole() == null) ? "" : m.getRole().trim();
                String content = (m.getContent() == null) ? "" : m.getContent().trim();
                if (content.isEmpty()) continue;
                if (!role.equals("user") && !role.equals("assistant")) continue;
                messages.add(Map.of("role", role, "content", content));
            }
        }

        // 2) 若绑定检测记录，则加一段上下文（DenseNet + 最新 AI 报告摘要）
        if (input.getBindRecordId() != null && input.getBindRecordId() > 0) {
            String ctx = buildRecordContextForChat(userId, input.getBindRecordId());
            if (!ctx.isEmpty()) {
                messages.add(Map.of("role", "system", "content", ctx));
            }
        }

        messages.add(Map.of("role", "user", "content", input.getQuestion().trim()));

        // 3) 调用文本模型
        String answer;
        try {
            QwenBailianClient.QwenResponse resp = qwenClient.chatText(messages);
            answer = resp.contentText();
        } catch (Exception ex) {
            throw new CustomException("AI 咨询失败，请稍后重试：" + ex.getMessage());
        }

        // 4) 输出：强制带免责声明
        FrontAIChatOutput out = new FrontAIChatOutput();
        out.setAnswer(answer);
        out.setDisclaimer("AI 答复仅供健康参考，不构成医疗诊断或个性化医疗建议，不能替代医生就诊。");
        return out;
    }

    // ---------------- helpers ----------------

    private String normalizeSpectrumType(String spectrumType) {
        if (!Extension.isNotNullOrEmpty(spectrumType)) return "MEL";
        String t = spectrumType.trim().toUpperCase();
        if (t.equals("MFCC") || t.equals("MEL") || t.equals("BOTH")) return t;
        return "MEL";
    }

    private TAudioAiReport findLatestSuccessReport(Integer userId, Integer recordId, String reportType, String spectrumType) {
        return audioAiReportMapper.selectOne(
                Wrappers.<TAudioAiReport>lambdaQuery()
                        .eq(TAudioAiReport::getIsDelete, false)
                        .eq(TAudioAiReport::getUserId, userId)
                        .eq(TAudioAiReport::getAudioScreenRecordId, recordId)
                        .eq(TAudioAiReport::getReportType, reportType)
                        .eq(TAudioAiReport::getSpectrumType, spectrumType)
                        .eq(TAudioAiReport::getStatus, "SUCCESS")
                        .orderByDesc(TAudioAiReport::getCreationTime)
                        .last("LIMIT 1")
        );
    }

    private byte[] loadSpectrumPngBytes(TAudioScreenRecordDto recordDto, String spectrumType) {
        // 仅实现：MEL 或 MFCC（BOTH 暂取 MEL；后续可升级为两张图拼接或两次调用）
        String url = null;
        if ("MFCC".equals(spectrumType)) {
            url = recordDto.getMfccSpectrumUrl();
        } else {
            url = recordDto.getMelSpectrumUrl();
        }
        if (!Extension.isNotNullOrEmpty(url)) {
            throw new CustomException("图谱未生成或URL为空，请先完成检测");
        }

        // 将 URL 映射为本地文件：external-resources/<folder>/<file>
        try {
            String[] parts = url.split("/");
            if (parts.length < 2) {
                throw new CustomException("图谱URL格式不正确：" + url);
            }
            String filename = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            String userDir = System.getProperty("user.dir");
            File f1 = new File(userDir + File.separator + "external-resources" + File.separator + folder, filename);
            if (!f1.exists()) {
                // 兼容：工作目录在 HealthControl.springboot
                File f2 = new File(new File(userDir).getParent() + File.separator + "external-resources" + File.separator + folder,
                        filename);
                if (f2.exists()) {
                    return Files.readAllBytes(f2.toPath());
                }
                throw new CustomException("找不到图谱文件：" + f1.getAbsolutePath());
            }
            return Files.readAllBytes(f1.toPath());
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CustomException("读取图谱失败：" + e.getMessage());
        }
    }

    private String buildSpectrumPrompt(TAudioScreenRecordDto record, String spectrumType) {
        String base = """
# Role & Task
你是“居家喉部健康管理系统”的高级 AI 嗓音评估专家。你将收到一张“喉部嗓音 %s 频谱图（由用户录音生成）”，同时可能附带声学数值指标（如 Jitter、Shimmer、HNR、MPT 等）。请综合视觉特征与数值变化，输出一份深度、易懂且极具指导意义的评估报告。

# Strict Constraints (严格红线)
1. 严禁做出医疗确诊，用词必须温和、客观，绝不能制造健康焦虑。
2. 输出必须且仅为一个合法的 JSON 字符串，严格遵守给定的 Key 结构。

# Content Enrichment Rules (内容丰富度与深度规则 - 必读)
在不改变 JSON Key 的前提下，通过以下方式让内容更饱满：
- **图数融合 (Data Fusion)**：在 `possible_explanations` 或 `key_observations` 中，必须将图谱特征与声学数值结合。例如：“📊 结合您的 HNR(信噪比，越高声音越干净) 偏低，且图谱显示谐波连贯性差，这双重印证了您当前可能存在发声漏气或声带闭合不全的情况。”（若无数值则说明缺失，不编造）。
- **优先级标记**：在 `at_home_tips` 和 `suggestions` 的数组中，使用 Emoji 区分建议的权重。例如：“🚨 核心干预：停止大声喊叫”、“💡 日常保养：多次少量饮水”。
- **闭环引导**：在 `suggestions` 和 `when_to_see_doctor` 中，主动提及小程序的特性。例如建议用户“将本次结果保存至【健康档案】，并开启【风险随访提醒】定期复查”。

# Output Format (严格遵循的 JSON 结构)
{
  "risk_level": "低|中|高",
  "plain_summary": "温和且综合的一句话总结（结合图谱与指标表现）",
  "at_home_tips": [
    "🚨 首要干预：...",
    "💡 日常保养：...",
    "🍏 饮食辅助：..."
  ],
  "key_observations": [
    "👁️ 频谱图特征1 + 📊 关联指标分析 -> 🗣️ 导致的声音表现",
    "👁️ 频谱图特征2 + 📊 关联指标分析 -> 🗣️ 导致的声音表现",
    "👁️ 频谱图特征3 -> 💡 独立声学推论",
    "👁️ 频谱图特征4 -> 💡 独立声学推论"
  ],
  "possible_explanations": [
    "【用声习惯溯源】...",
    "【生理状态评估】..."
  ],
  "suggestions": [
    "具体执行建议1（结合小程序功能，如加入【健康档案】观察追踪）",
    "具体执行建议2"
  ],
  "when_to_see_doctor": [
    "🔴 红旗指征：出现什么具体症状（如咳血、呼吸受阻）必须立即就医",
    "🏥 就诊建议：建议使用小程序【就医辅助】功能，并向医生出示本软件记录的音频与症状日志"
  ],
  "retest_tips": [
    "🎙️ 录音环境优化：...",
    "🗣️ 发音标准校准：..."
  ],
  "disclaimer": "⚠️ 提示：以上 AI 答复及声学多维特征分析仅供健康参考，不构成专业医疗诊断。如有不适或症状持续，请及时前往正规医院耳鼻喉科（推荐进行电子喉镜检查）就诊。"
}
""".formatted(spectrumType);

        // 增加检测记录上下文（帮助模型更贴合“本次记录”，减少模板化）
        StringBuilder ctx = new StringBuilder();
        if (record.getPrimaryScreenResult() != null) {
            boolean malignant = Boolean.TRUE.equals(record.getPrimaryScreenResult());
            String label = malignant ? "异常风险（恶性侧）" : "相对正常（良性侧）";
            ctx.append("\n补充上下文（来自本系统的检测记录，供参考）：\n");
            ctx.append("- 初筛结果：").append(label).append("\n");
            if (record.getPrimaryScreenConfidence() != null) {
                ctx.append("- 初筛置信度：").append(record.getPrimaryScreenConfidence()).append("\n");
            }
        }
        // 关键声学指标（可能为空）
        if (record.getJitter() != null || record.getShimmer() != null || record.getHnr() != null || record.getMpt() != null) {
            if (ctx.length() == 0) ctx.append("\n补充上下文（来自本系统的检测记录，供参考）：\n");
            if (record.getJitter() != null) ctx.append("- Jitter：").append(record.getJitter()).append("\n");
            if (record.getShimmer() != null) ctx.append("- Shimmer：").append(record.getShimmer()).append("\n");
            if (record.getHnr() != null) ctx.append("- HNR：").append(record.getHnr()).append("\n");
            if (record.getMpt() != null) ctx.append("- MPT：").append(record.getMpt()).append("\n");
        }
        // 录音基本信息
        if (record.getAudioDuration() != null || record.getAudioSamplingRate() != null) {
            if (ctx.length() == 0) ctx.append("\n补充上下文（来自本系统的检测记录，供参考）：\n");
            if (record.getAudioDuration() != null) ctx.append("- 录音时长(s)：").append(record.getAudioDuration()).append("\n");
            if (record.getAudioSamplingRate() != null) ctx.append("- 采样率(Hz)：").append(record.getAudioSamplingRate()).append("\n");
        }
        return base + ctx;
    }

    private String buildUploadSpectrumPrompt(String spectrumType) {
        return """
# Role & Task
你是“居家喉部健康管理系统”的 AI 嗓音分析引擎。你将收到一张用户上传的“喉部嗓音 %s 频谱图”，请结合图谱视觉信号特征，输出一份面向普通人的、内容丰满的健康风险初步解读报告。

# Strict Constraints (严格红线)
1. 你不是医生，严禁使用“确诊”、“必定是XX病变（如息肉/小结）”等绝对性诊断词汇。
2. 输出必须且仅为一个合法的 JSON 字符串，绝不能包含 Markdown 代码块标记 (```json) 外的内容，严格保留指定的 Key 不变。

# Content Enrichment Rules (内容丰富度规则 - 必读)
在保持 JSON Key 不变的前提下，请丰富 Value 的内容层次：
- `at_home_tips` 和 `suggestions`：每个字符串请以【维度标签】开头，例如：“【发声习惯】尽量避免清嗓子，这会导致声带剧烈摩擦；”、“【环境湿度】建议在卧室使用加湿器，保持湿度在 50%% 左右”。
- `key_observations`：必须采用“👁️ 视觉现象 -> 💡 声学推论 -> 🗣️ 听觉表现”的结构。例如：“图谱高频区域出现明显噪声底（现象），提示发声时声门可能闭合不全（推论），这在听感上通常表现为声音漏气或沙哑（表现）。”
- 所有专业术语（谐波、共振峰、底噪）必须在句中用括号 `(即：...)` 给出生活化解释。

# Output Format (严格遵循的 JSON 结构)
{
  "risk_level": "低|中|高",
  "plain_summary": "一句话温和总结（例如：当前图谱显示您的声音底噪略高，提示声带可能处于疲劳或轻度充血状态，不必过于焦虑，请多给嗓子放个假。）",
  "at_home_tips": [
    "【发声习惯】...",
    "【饮食调整】...",
    "【环境干预】..."
  ],
  "key_observations": [
    "👁️ 能量分布特征 -> 💡 推论 -> 🗣️ 听感表现",
    "👁️ 谐波清晰度 -> 💡 推论 -> 🗣️ 听感表现",
    "👁️ 噪声底(底噪)情况 -> 💡 推论 -> 🗣️ 听感表现",
    "👁️ 周期稳定性 -> 💡 推论 -> 🗣️ 听感表现"
  ],
  "possible_explanations": [
    "【生理可能】...",
    "【行为可能】..."
  ],
  "suggestions": [
    "结合图谱给出的具体日常保养动作1",
    "结合图谱给出的具体日常保养动作2"
  ],
  "when_to_see_doctor": [
    "🚨 若出现以下情况之一（如连续嘶哑超14天、吞咽痛），请尽快就医",
    "💡 您可以通过小程序底部的【就医辅助】模块查找附近的耳鼻喉科专家"
  ],
  "retest_tips": [
    "🎙️ 环境建议：...",
    "🗣️ 发音建议：..."
  ],
  "disclaimer": "⚠️ 提示：以上 AI 答复与频谱分析仅基于声学特征，供健康参考与日常预警，绝不构成任何医疗诊断。如症状持续或加重，请务必及时前往正规医院耳鼻喉科就医检查。"
}
""".formatted(spectrumType);
    }

    private ParseResult parseReportJson(String modelText) {
        if (modelText == null) modelText = "";
        String raw = modelText.trim();
        if (raw.isEmpty()) {
            return new ParseResult("FAILED", null, null, "模型返回为空");
        }
        // 尝试抽取最后一个 JSON
        int lastL = raw.lastIndexOf('{');
        int lastR = raw.lastIndexOf('}');
        String json = raw;
        if (lastL >= 0 && lastR > lastL) {
            json = raw.substring(lastL, lastR + 1);
        }
        try {
            JsonNode node = mapper.readTree(json);
            String riskLevel = node.path("risk_level").asText("");
            String disclaimer = node.path("disclaimer").asText("");
            return new ParseResult("SUCCESS", json, normalizeRiskLevel(riskLevel), disclaimer);
        } catch (Exception e) {
            return new ParseResult("FAILED", null, null, "JSON解析失败");
        }
    }

    private String normalizeRiskLevel(String riskLevelText) {
        if (riskLevelText == null) return null;
        String t = riskLevelText.trim();
        if (t.contains("低")) return "LOW";
        if (t.contains("中")) return "MEDIUM";
        if (t.contains("高")) return "HIGH";
        // 兜底：英文
        t = t.toUpperCase();
        if (t.contains("LOW")) return "LOW";
        if (t.contains("MEDIUM")) return "MEDIUM";
        if (t.contains("HIGH")) return "HIGH";
        return null;
    }

    private record ParseResult(String parseStatus, String reportJson, String riskLevel, String disclaimer) {}

    private TAudioAiReport persistReportSuccess(Integer userId, Integer recordId, String reportType, String spectrumType,
                                               int latencyMs, ParseResult parse, String rawRequest, String rawResponse) {
        TAudioAiReportDto dto = new TAudioAiReportDto();
        dto.setUserId(userId);
        dto.setAudioScreenRecordId(recordId);
        dto.setReportType(reportType);
        dto.setSpectrumType(spectrumType);
        dto.setModelProvider(PROVIDER);
        dto.setModelName("qwen-vl");
        dto.setModelVersion(null);
        dto.setStatus("SUCCESS");
        dto.setParseStatus(parse.parseStatus());
        dto.setRiskLevel(parse.riskLevel());
        dto.setRiskScore(null);
        dto.setLatencyMs(latencyMs);
        dto.setRawRequest(rawRequest);
        dto.setRawResponse(rawResponse);
        dto.setErrorCode(null);
        dto.setErrorMessage(null);
        dto.setIsDelete(false);

        // Summary：如果解析成功则简单拼一个摘要，否则固定模板
        if ("SUCCESS".equals(parse.parseStatus()) && parse.riskLevel() != null) {
            dto.setSummaryText("AI 解读完成，风险等级：" + parse.riskLevel());
            dto.setReportJson(parse.reportJson());
        } else {
            dto.setSummaryText("AI 解读已生成，但结构化解析失败，可查看原始文本或重试。");
            dto.setReportJson(rawResponse);
        }

        TAudioAiReportDto savedDto = audioAiReportService.CreateOrEdit(dto);
        TAudioAiReport entity = audioAiReportMapper.selectById(savedDto.getId());
        return entity;
    }

    private TAudioAiReport persistReportFail(Integer userId, Integer recordId, String reportType, String spectrumType,
                                            int latencyMs, String errorCode, String errorMessage,
                                            String rawRequest, String rawResponse) {
        TAudioAiReportDto dto = new TAudioAiReportDto();
        dto.setUserId(userId);
        dto.setAudioScreenRecordId(recordId);
        dto.setReportType(reportType);
        dto.setSpectrumType(spectrumType);
        dto.setModelProvider(PROVIDER);
        dto.setModelName("qwen-vl");
        dto.setModelVersion(null);
        dto.setStatus("FAILED");
        dto.setParseStatus("FAILED");
        dto.setRiskLevel(null);
        dto.setRiskScore(null);
        dto.setLatencyMs(latencyMs);
        dto.setRawRequest(rawRequest);
        dto.setRawResponse(rawResponse);
        dto.setErrorCode(errorCode);
        dto.setErrorMessage(errorMessage);
        dto.setSummaryText("AI 解读失败：" + (errorMessage == null ? "" : errorMessage));
        dto.setReportJson(null);
        dto.setIsDelete(false);
        TAudioAiReportDto savedDto = audioAiReportService.CreateOrEdit(dto);
        return audioAiReportMapper.selectById(savedDto.getId());
    }

    private FrontAIReportOutput toOutput(TAudioAiReport entity) {
        FrontAIReportOutput out = new FrontAIReportOutput();
        out.setReportId(entity.getId());
        out.setRecordId(entity.getAudioScreenRecordId());
        out.setReportType(entity.getReportType());
        out.setSpectrumType(entity.getSpectrumType());
        out.setRiskLevel(entity.getRiskLevel());
        out.setRiskScore(entity.getRiskScore());
        out.setSummaryText(entity.getSummaryText());
        out.setReportJson(entity.getReportJson());
        out.setCreatedTime(entity.getCreationTime() != null ? entity.getCreationTime().toString() : null);
        out.setModelName(entity.getModelName());
        out.setStatus(entity.getStatus());
        out.setErrorCode(entity.getErrorCode());
        out.setErrorMessage(entity.getErrorMessage());
        return out;
    }

    private void writeCallLog(Integer detectId, String spectrumType, boolean ok, int latencyMs, String summary, String failDetail) {
        try {
            TModelInterfaceCallLog log = new TModelInterfaceCallLog();
            log.setDetectId(detectId);
            log.setUpdateTime(LocalDateTime.now());
            log.setCallLink(null);
            log.setModelInterfaceType(Boolean.TRUE);
            log.setModelVersion("qwen-vl");
            log.setServiceName("audio_ai_qwen_vl");
            log.setInputSpectrumUrl(spectrumType);
            log.setInputParams("ai_report");
            log.setCallCost(latencyMs);
            log.setQualityLevel(latencyMs <= 10000 ? 0 : latencyMs <= 30000 ? 1 : 2);
            log.setResultSummary(summary);
            log.setResultComplete(null);
            log.setCallStatus(ok);
            log.setFailErrorCode(ok ? null : "AI_CALL_FAILED");
            log.setFailDetailInfo(ok ? null : failDetail);
            log.setIsDelete(false);
            modelInterfaceCallLogMapper.insert(log);
        } catch (Exception ignore) {
        }
    }

    private String buildMedicalChatSystemPrompt() {
        return """
# Role
你是“居家喉部健康管理小程序”的专属“喉部健康咨询助手”。你不仅具备扎实的嗓音健康科普知识，还能像一位有同理心的健康管家一样，为普通用户提供温和、清晰、结构化的日常护嗓与就医建议。

# Constraints (绝对红线)
1. 身份限制：你绝不是医生，严禁做出任何“确诊”、“必定是XX病”的医疗诊断，绝不能替代医生面诊与喉镜检查。
2. 用药红线：严禁提供任何具体的处方药物推荐、用药频次或剂量建议。
3. 态度基调：必须共情用户的焦虑（如声音嘶哑带来的困扰），语气温和、客观、不恐吓。
4. 强制免责：每次回答的末尾，必须独立一段附带：“⚠️ **免责声明**：以上 AI 答复与分析仅供健康科普与日常保养参考，不构成专业医疗诊断。如症状持续超过两周或伴有呼吸/吞咽困难，请务必前往正规医院耳鼻喉科就诊。”

# Content & Formatting Guidelines (内容与排版指南)
1. 结构化输出：使用分段、Markdown 列表（如 `-` 或 `1.`）和加粗（`**重点**`）来增强可读性，避免大段密集的文字。
2. 贴近生活与实操：给出的建议必须具体。不要只说“多喝水”，要说“少量多次饮用温水，保持咽喉湿润”。
3. 联动小程序功能：在合适的上下文中，主动引导用户使用小程序内的功能。例如：
   - 提到饮食禁忌时，建议：“您可以使用小程序内的【饮食管理】查看对嗓音友好的食物推荐。”
   - 提到持续不适时，建议：“建议您在【健康档案】或【症状日志】中记录每日变化，方便日后就医时供医生参考。”
""";
    }

    private String buildRecordContextForChat(Integer userId, Integer recordId) {
        try {
            TAudioScreenRecordPagedInput getInput = new TAudioScreenRecordPagedInput();
            getInput.setId(recordId);
            getInput.setUserId(userId);
            getInput.setPage(1L);
            getInput.setLimit(1L);
            TAudioScreenRecordDto dto = audioScreenRecordService.Get(getInput);
            if (dto == null || dto.getId() == null) return "";

            // 最新 AI 报告摘要
            TAudioAiReport latest = findLatestSuccessReport(userId, recordId, REPORT_TYPE_RECORD, "MEL");
            String reportSummary = latest != null ? latest.getSummaryText() : "";

            return "用户最近一次嗓音检测上下文（仅供参考）："
                    + "\n- 初筛结果：" + (Boolean.TRUE.equals(dto.getPrimaryScreenResult()) ? "异常风险" : "相对正常")
                    + (dto.getPrimaryScreenConfidence() != null ? "\n- 初筛置信度：" + dto.getPrimaryScreenConfidence() : "")
                    + (Extension.isNotNullOrEmpty(reportSummary) ? "\n- AI解读摘要：" + reportSummary : "");
        } catch (Exception ignore) {
            return "";
        }
    }
}

