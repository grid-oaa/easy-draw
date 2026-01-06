package com.easydraw.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class ModelTestRequest {
  @NotNull(message = "modelConfig is required")
  @Valid
  private ModelConfig modelConfig;
  private String prompt;

  public ModelConfig getModelConfig() {
    return modelConfig;
  }

  public void setModelConfig(ModelConfig modelConfig) {
    this.modelConfig = modelConfig;
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }
}



