package com.easydraw.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class GenerateDiagramRequest {

  /**
   * 绘图语言：mermaid / plantuml（可扩展），为空默认 mermaid。
   */
  private String language;

  @NotBlank
  private String diagramType;

  @NotBlank
  private String prompt;

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
}

