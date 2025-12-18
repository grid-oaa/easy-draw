package com.easydraw.backend.diagram.strategy;

import com.easydraw.backend.ai.AiClient;
import com.easydraw.backend.diagram.DiagramGenerationInput;
import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.diagram.DiagramLanguageStrategy;
import com.easydraw.backend.dto.DiagramError;
import com.easydraw.backend.plantuml.PlantUmlValidator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PlantUmlStrategy implements DiagramLanguageStrategy {

  private final AiClient aiClient;
  private final PlantUmlValidator plantUmlValidator;

  public PlantUmlStrategy(AiClient aiClient, PlantUmlValidator plantUmlValidator) {
    this.aiClient = aiClient;
    this.plantUmlValidator = plantUmlValidator;
  }

  @Override
  public DiagramLanguage language() {
    return DiagramLanguage.PLANTUML;
  }

  @Override
  public String generate(DiagramGenerationInput input) {
    return aiClient.generate(input.getLanguage(), input.getDiagramType(), input.getPrompt());
  }

  @Override
  public List<DiagramError> validate(String content, DiagramGenerationInput input) {
    return plantUmlValidator.validate(content);
  }
}

