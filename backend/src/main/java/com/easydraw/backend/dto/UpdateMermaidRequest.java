package com.easydraw.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateMermaidRequest {

  @NotBlank private String prompt;

  @NotBlank private String mermaid;

  private String diagramType;

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getMermaid() {
    return mermaid;
  }

  public void setMermaid(String mermaid) {
    this.mermaid = mermaid;
  }

  public String getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(String diagramType) {
    this.diagramType = diagramType;
  }
}
