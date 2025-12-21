package com.easydraw.backend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bigmodel")
public class BigModelProperties {

  /**
   * API 基础地址，例如：https://open.bigmodel.cn/api/paas/v4/chat/completions
   */
  private String baseUrl;

  /**
   * API Key，需在配置中设置或以环境变量传入（如 BIGMODEL_API_KEY）。
   */
  private String apiKey;

  /**
   * 模型名称，默认 glm-4.6v-flash。
   */
  private String model = "glm-4.6v-flash";

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }
}

