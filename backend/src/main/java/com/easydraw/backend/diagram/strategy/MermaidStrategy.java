package com.easydraw.backend.diagram.strategy;

import com.easydraw.backend.ai.AiClient;
import com.easydraw.backend.diagram.DiagramGenerationInput;
import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.diagram.DiagramLanguageStrategy;
import com.easydraw.backend.dto.DiagramError;
import com.easydraw.backend.mermaid.MermaidValidator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MermaidStrategy implements DiagramLanguageStrategy {

  private final AiClient aiClient;
  private final MermaidValidator mermaidValidator;

  public MermaidStrategy(AiClient aiClient, MermaidValidator mermaidValidator) {
    this.aiClient = aiClient;
    this.mermaidValidator = mermaidValidator;
  }

  @Override
  public DiagramLanguage language() {
    return DiagramLanguage.MERMAID;
  }

  @Override
  public String generate(DiagramGenerationInput input) {
    return aiClient.generate(
        input.getLanguage(), input.getDiagramType(), input.getPrompt(), input.getModelConfig());
  }

  @Override
  public List<DiagramError> validate(String content, DiagramGenerationInput input) {
    return mermaidValidator.validate(content, input.getDiagramType());
  }
}
