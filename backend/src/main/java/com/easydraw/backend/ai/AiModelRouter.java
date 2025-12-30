package com.easydraw.backend.ai;

import com.easydraw.backend.dto.ModelConfig;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

@Component
public class AiModelRouter {

  private final EasyDrawAiProperties props;

  public AiModelRouter(EasyDrawAiProperties props) {
    this.props = props;
  }

  public String resolveProvider(ModelConfig modelConfig) {
    ModelConfig effectiveConfig = props.isAllowRequestConfig() ? modelConfig : null;
    String provider =
        pick(effectiveConfig == null ? null : effectiveConfig.getProvider(), props.getProvider());
    return provider == null ? "openai" : provider;
  }

  public OpenAiChatOptions resolveOptions(ModelConfig modelConfig) {
    ModelConfig effectiveConfig = modelConfig;
    if (!props.isAllowRequestConfig()) {
      effectiveConfig = null;
    }

    String model = pick(effectiveConfig == null ? null : effectiveConfig.getModel(), props.getModel());
    Double temperature =
        pick(effectiveConfig == null ? null : effectiveConfig.getTemperature(), props.getTemperature());
    Integer maxTokens =
        pick(effectiveConfig == null ? null : effectiveConfig.getMaxTokens(), props.getMaxTokens());

    if (model == null && temperature == null && maxTokens == null) {
      return null;
    }

    OpenAiChatOptions options = new OpenAiChatOptions();
    BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(options);
    if (model != null) {
      wrapper.setPropertyValue("model", model);
    }
    if (temperature != null) {
      wrapper.setPropertyValue("temperature", temperature);
    }
    if (maxTokens != null) {
      wrapper.setPropertyValue("maxTokens", maxTokens);
    }
    return options;
  }

  private static <T> T pick(T primary, T fallback) {
    return primary == null ? fallback : primary;
  }
}
