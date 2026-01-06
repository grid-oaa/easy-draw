package com.easydraw.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GenerateDiagramRequest {

  /**
   * 绘图语言：mermaid / plantuml（可扩展），为空默认 mermaid。
   */
  private String language;

  /**
   *
   */
  private String diagramType;

  /**
   * 用户输入
   */
  @NotBlank
  private String prompt;

  /**
   * 大模型配置
   */
  @NotNull
  @Valid
  private ModelConfig modelConfig;

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(String diagramType) {
    this.diagramType = diagramType;
  }

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



