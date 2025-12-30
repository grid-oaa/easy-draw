package com.easydraw.backend.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "easy-draw.ai")
public class EasyDrawAiProperties {

  private String client = "spring-ai";
  private String provider = "openai";
  private String model;
  private Double temperature;
  private Integer maxTokens;
  private boolean allowRequestConfig = true;

  public String getClient() {
    return client;
  }

  public void setClient(String client) {
    this.client = client;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public Double getTemperature() {
    return temperature;
  }

  public void setTemperature(Double temperature) {
    this.temperature = temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public void setMaxTokens(Integer maxTokens) {
    this.maxTokens = maxTokens;
  }

  public boolean isAllowRequestConfig() {
    return allowRequestConfig;
  }

  public void setAllowRequestConfig(boolean allowRequestConfig) {
    this.allowRequestConfig = allowRequestConfig;
  }
}
