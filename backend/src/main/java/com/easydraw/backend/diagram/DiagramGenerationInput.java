package com.easydraw.backend.diagram;

import com.easydraw.backend.dto.ModelConfig;

public class DiagramGenerationInput {

  private final DiagramLanguage language;
  private final String diagramType;
  private final String prompt;
  private final ModelConfig modelConfig;

  public DiagramGenerationInput(
      DiagramLanguage language, String diagramType, String prompt, ModelConfig modelConfig) {
    this.language = language;
    this.diagramType = diagramType;
    this.prompt = prompt;
    this.modelConfig = modelConfig;
  }

  public DiagramLanguage getLanguage() {
    return language;
  }

  public String getDiagramType() {
    return diagramType;
  }

  public String getPrompt() {
    return prompt;
  }

  public ModelConfig getModelConfig() {
    return modelConfig;
  }
}
