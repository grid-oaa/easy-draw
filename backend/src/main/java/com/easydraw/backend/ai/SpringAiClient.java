package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.dto.ModelConfig;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "easy-draw.ai.client", havingValue = "spring-ai", matchIfMissing = true)
public class SpringAiClient implements AiClient {

  private static final Logger log = LoggerFactory.getLogger(SpringAiClient.class);

  private final AiModelRouter modelRouter;

  public SpringAiClient(AiModelRouter modelRouter) {
    this.modelRouter = modelRouter;
  }

  @Override
  public String generate(
      DiagramLanguage language, String diagramType, String prompt, ModelConfig modelConfig) {
    String provider = modelRouter.resolveProvider(modelConfig);
    if (!isOpenAiCompatible(provider)) {
      throw new IllegalStateException("不支持的模型提供方: " + provider);
    }

    EffectiveConfig config = resolveConfig(modelConfig);
    String mergedPrompt = buildPrompt(language, diagramType, prompt);
    OpenAiChatOptions options = modelRouter.resolveOptions(modelConfig);

    try {
      OpenAiApi api =
          new OpenAiApi(
              config.baseUrl,
              config.apiKey,
              config.completionsPath,
              config.embeddingsPath,
              RestClient.builder(),
              WebClient.builder(),
              RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
      OpenAiChatModel chatModel = new OpenAiChatModel(api);
      Prompt chatPrompt = buildChatPrompt(language, mergedPrompt, options);
      String content = chatModel.call(chatPrompt).getResult().getOutput().getText();
      if (content == null || content.isBlank()) {
        throw new IllegalStateException("大模型未返回内容");
      }
      return content.trim();
    } catch (Exception e) {
      log.error("Spring AI 调用异常, provider={}", provider, e);
      throw new IllegalStateException("调用大模型异常: " + e.getMessage(), e);
    }
  }

  private Prompt buildChatPrompt(
      DiagramLanguage language, String mergedPrompt, OpenAiChatOptions options) {
    List<Message> messages =
        List.of(new SystemMessage(systemPrompt(language)), new UserMessage(mergedPrompt));
    if (options == null) {
      return new Prompt(messages);
    }
    return new Prompt(messages, options);
  }

  private EffectiveConfig resolveConfig(ModelConfig modelConfig) {
    String baseUrl = modelConfig == null ? null : modelConfig.getBaseUrl();
    String apiKey = modelConfig == null ? null : modelConfig.getApiKey();
    if (isBlank(baseUrl) || isBlank(apiKey)) {
      throw new IllegalStateException("缺少模型配置：请在请求中提供 baseUrl 与 apiKey");
    }

    String normalizedBaseUrl = normalizeBaseUrl(baseUrl.trim());
    if (isBlank(normalizedBaseUrl)) {
      throw new IllegalStateException("baseUrl 无效，请检查是否包含版本路径");
    }
    return new EffectiveConfig(normalizedBaseUrl, apiKey.trim(), "/chat/completions", "/embeddings");
  }

  private boolean isOpenAiCompatible(String provider) {
    if (provider == null || provider.isBlank()) {
      return true;
    }
    String normalized = provider.trim().toLowerCase();
    return normalized.equals("openai") || normalized.equals("openai-compatible");
  }

  private String buildPrompt(DiagramLanguage language, String diagramType, String prompt) {
    String type = diagramType == null ? "" : diagramType;
    return """
        请使用纯 %s 语法输出一个图表，不要添加额外解释或包裹，确保语法可直接渲染。注意：业务描述如果使用的是中文，图表也要使用中文描述。
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

  private String normalizeBaseUrl(String baseUrl) {
    int idx = baseUrl.indexOf("/chat/completions");
    String normalized = idx > 0 ? baseUrl.substring(0, idx) : baseUrl;
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private static boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private static class EffectiveConfig {
    private final String baseUrl;
    private final String apiKey;
    private final String completionsPath;
    private final String embeddingsPath;

    private EffectiveConfig(
        String baseUrl, String apiKey, String completionsPath, String embeddingsPath) {
      this.baseUrl = baseUrl;
      this.apiKey = apiKey;
      this.completionsPath = completionsPath;
      this.embeddingsPath = embeddingsPath;
    }
  }
}
