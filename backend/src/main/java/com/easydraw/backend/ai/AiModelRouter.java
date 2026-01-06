package com.easydraw.backend.ai;

import com.easydraw.backend.dto.ModelConfig;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

@Component
public class AiModelRouter {

  public String resolveProvider(ModelConfig modelConfig) {
    if (modelConfig == null) {
      return "openai";
    }
    String provider = modelConfig.getProvider();
    return provider == null || provider.trim().isEmpty() ? "openai" : provider;
  }

  public OpenAiChatOptions resolveOptions(ModelConfig modelConfig) {
    if (modelConfig == null) {
      return null;
    }

    String model = modelConfig.getModel();
    Double temperature = modelConfig.getTemperature();
    Integer maxTokens = modelConfig.getMaxTokens();

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
}
