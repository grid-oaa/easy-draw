package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.dto.ModelConfig;

public interface AiClient {
  String generate(DiagramLanguage language, String diagramType, String prompt, ModelConfig modelConfig);
}
