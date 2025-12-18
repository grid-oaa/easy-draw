package com.easydraw.backend.service;

import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;

public interface DiagramGenerationService {
  GenerateDiagramResponse generate(GenerateDiagramRequest request);
}

