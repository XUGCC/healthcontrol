package com.example.web.tools.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Qwen（百炼/通义千问）调用客户端
 *
 * 说明：
 * - 优先使用“兼容模式（OpenAI Chat Completions）”请求格式，便于多模态图像输入（data:image/png;base64,...）
 * - 具体参数以百炼控制台文档为准：
 *   - 文档：`https://bailian.console.aliyun.com/cn-beijing/?tab=doc#/doc`
 *   - API：`https://bailian.console.aliyun.com/cn-beijing/?tab=api#/api`
 */
@Component
public class QwenBailianClient {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.qwen.api-key:}")
    private String apiKey;

    /**
     * OpenAI compatible endpoint (DashScope compatible-mode)
     */
    @Value("${ai.qwen.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String baseUrl;

    @Value("${ai.qwen.vl-model:qwen-vl-plus}")
    private String vlModel;

    @Value("${ai.qwen.text-model:qwen-plus}")
    private String textModel;

    public record QwenResponse(String rawResponse, String contentText, Integer totalTokens) {}

    /**
     * 多模态：文本 + PNG base64（不含 data: 前缀）
     */
    public QwenResponse chatWithPngBase64(String prompt, String pngBase64) throws Exception {
        ensureKey();
        String dataUrl = "data:image/png;base64," + pngBase64;

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("role", "user");
        message.put("content", List.of(
                Map.of("type", "text", "text", prompt),
                Map.of("type", "image_url", "image_url", Map.of("url", dataUrl))
        ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", vlModel);
        body.put("messages", List.of(message));
        // 适度提高随机性，减少“模板化”措辞，同时仍保持结构化 JSON 输出稳定
        body.put("temperature", 0.55);
        body.put("top_p", 0.9);
        // 降低重复（对“模板化/套话”有帮助）
        body.put("presence_penalty", 0.2);
        body.put("frequency_penalty", 0.2);

        return post(body);
    }

    /**
     * 文本对话（医疗咨询）：仅文本
     */
    public QwenResponse chatText(List<Map<String, Object>> messages) throws Exception {
        ensureKey();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", textModel);
        body.put("messages", messages);
        body.put("temperature", 0.5);
        body.put("top_p", 0.9);
        body.put("presence_penalty", 0.2);
        body.put("frequency_penalty", 0.2);
        return post(body);
    }

    private QwenResponse post(Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String raw = resp.body();
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("Qwen API 调用失败，status=" + resp.statusCode() + ", body=" + raw);
        }

        JsonNode root = mapper.readTree(raw);
        String content = "";
        Integer totalTokens = null;
        try {
            JsonNode choice0 = root.path("choices").get(0);
            if (choice0 != null) {
                content = choice0.path("message").path("content").asText("");
            }
            JsonNode usage = root.path("usage");
            if (usage != null && usage.has("total_tokens")) {
                totalTokens = usage.get("total_tokens").asInt();
            }
        } catch (Exception ignore) {
        }
        return new QwenResponse(raw, content, totalTokens);
    }

    private void ensureKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("未配置 ai.qwen.api-key，请在环境变量或 application.yml 中配置");
        }
    }
}

