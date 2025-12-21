package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@Primary
@ConditionalOnProperty(prefix = "bigmodel", name = "api-key")
public class BigModelAiClient implements AiClient {

  private static final Logger log = LoggerFactory.getLogger(BigModelAiClient.class);

  private final BigModelProperties props;
  private final WebClient client;

  public BigModelAiClient(BigModelProperties props) {
    this.props = props;
    this.client =
        WebClient.builder()
            .baseUrl(props.getBaseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
            .build();
  }

  @Override
  public String generate(DiagramLanguage language, String diagramType, String prompt) {
    String mergedPrompt = buildPrompt(language, diagramType, prompt);

    Map<String, Object> body = new HashMap<>();
    body.put("model", props.getModel());
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
