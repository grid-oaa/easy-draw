package com.easydraw.backend.diagram;

public class DiagramGenerationInput {

  private final DiagramLanguage language;
  private final String diagramType;
  private final String prompt;

  public DiagramGenerationInput(DiagramLanguage language, String diagramType, String prompt) {
    this.language = language;
    this.diagramType = diagramType;
    this.prompt = prompt;
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
}

