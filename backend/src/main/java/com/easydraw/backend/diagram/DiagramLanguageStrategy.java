package com.easydraw.backend.diagram;

import com.easydraw.backend.dto.DiagramError;
import java.util.List;

public interface DiagramLanguageStrategy {
  DiagramLanguage language();

  String generate(DiagramGenerationInput input);

  List<DiagramError> validate(String content, DiagramGenerationInput input);
}

