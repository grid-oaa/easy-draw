package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.dto.ModelConfig;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@ConditionalOnProperty(name = "easy-draw.ai.client", havingValue = "legacy-http")
public class BigModelAiClient implements AiClient {

  private static final Logger log = LoggerFactory.getLogger(BigModelAiClient.class);

  private final BigModelProperties props;
  public BigModelAiClient(BigModelProperties props) {
    this.props = props;
  }

  @Override
  public String generate(
      DiagramLanguage language, String diagramType, String prompt, ModelConfig modelConfig) {
    EffectiveConfig config = resolveConfig(modelConfig);
    WebClient client =
        WebClient.builder()
            .baseUrl(config.baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey)
            .build();

    String mergedPrompt = buildPrompt(language, diagramType, prompt);

    Map<String, Object> body = new HashMap<>();
    body.put("model", config.model);
    body.put(
        "messages",
        List.of(
            Map.of("role", "system", "content", systemPrompt(language)),
            Map.of("role", "user", "content", mergedPrompt)));

    try {
      BigModelResponse resp =
          client
              .post()
              .bodyValue(body)
              .retrieve()
              .bodyToMono(BigModelResponse.class)
              .timeout(Duration.ofSeconds(20))
              .block();

      if (resp == null || resp.choices == null || resp.choices.isEmpty()) {
        throw new IllegalStateException("大模型响应为空");
      }
      String content = resp.choices.get(0).message.content;
      if (content == null || content.isBlank()) {
        throw new IllegalStateException("大模型未返回内容");
      }
      return content.trim();
    } catch (WebClientResponseException e) {
      log.error("BigModel API 调用失败: {}", e.getResponseBodyAsString(), e);
      throw new IllegalStateException("调用大模型失败：" + e.getStatusCode());
    } catch (Exception e) {
      log.error("BigModel API 调用异常", e);
      throw new IllegalStateException("调用大模型异常：" + e.getMessage(), e);
    }
  }

  private String buildPrompt(DiagramLanguage language, String diagramType, String prompt) {
    String type = diagramType == null ? "" : diagramType;
    return """
        请使用纯 %s 语法输出一个图表，不要添加额外解释或包裹，确保语法可直接渲染。注意：业务描述如果使用的中文，图表也要使用中文描述。
        图表类型/方向偏好：%s
        业务描述：%s
        """
        .formatted(language.getCode(), type, prompt);
  }

  private String systemPrompt(DiagramLanguage language) {
    if (language == DiagramLanguage.PLANTUML) {
      return "你是图表生成助手，只输出 PlantUML 代码，不要额外文字。";
    }
    return "你是图表生成助手，只输出 Mermaid 代码，不要额外文字。";
  }

  private EffectiveConfig resolveConfig(ModelConfig modelConfig) {
    String baseUrl =
        pick(modelConfig == null ? null : modelConfig.getBaseUrl(), props.getBaseUrl());
    String apiKey = pick(modelConfig == null ? null : modelConfig.getApiKey(), props.getApiKey());
    String model = pick(modelConfig == null ? null : modelConfig.getModel(), props.getModel());

    if (isBlank(baseUrl) || isBlank(apiKey)) {
      throw new IllegalStateException("Missing model configuration.");
    }
    if (isBlank(model)) {
      model = "glm-4.6v-flash";
    }
    return new EffectiveConfig(baseUrl, apiKey, model);
  }

  private static String pick(String primary, String fallback) {
    return isBlank(primary) ? fallback : primary;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static class EffectiveConfig {
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    private EffectiveConfig(String baseUrl, String apiKey, String model) {
      this.baseUrl = baseUrl;
      this.apiKey = apiKey;
      this.model = model;
    }
  }

  // DTO for response
  private static class BigModelResponse {
    public List<Choice> choices;
  }

  private static class Choice {
    public Message message;
  }

  private static class Message {
    public String role;
    public String content;
  }
}
