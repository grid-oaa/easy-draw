package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;

public interface AiClient {
  String generate(DiagramLanguage language, String diagramType, String prompt);
}
