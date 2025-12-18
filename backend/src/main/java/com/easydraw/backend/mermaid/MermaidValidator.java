package com.easydraw.backend.mermaid;

import com.easydraw.backend.dto.DiagramError;
import java.util.List;

public interface MermaidValidator {
  List<DiagramError> validate(String mermaid, String diagramType);
}

