package com.easydraw.backend.service;

import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;
import com.easydraw.backend.dto.UpdateMermaidRequest;

public interface DiagramGenerationService {
  GenerateDiagramResponse generate(GenerateDiagramRequest request);

  GenerateDiagramResponse editMermaid(UpdateMermaidRequest request);
}
