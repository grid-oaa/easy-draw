package com.easydraw.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class StyleModifyRequest {

  @NotBlank private String prompt;
  private ModelConfig modelConfig;

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public ModelConfig getModelConfig() {
    return modelConfig;
  }

  public void setModelConfig(ModelConfig modelConfig) {
    this.modelConfig = modelConfig;
  }
}