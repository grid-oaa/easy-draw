package com.easydraw.backend.api;

import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;
import com.easydraw.backend.dto.UpdateMermaidRequest;
import com.easydraw.backend.service.DiagramGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

  private final DiagramGenerationService diagramGenerationService;

  public AiController(DiagramGenerationService diagramGenerationService) {
    this.diagramGenerationService = diagramGenerationService;
  }

  @PostMapping("/diagram")
  public GenerateDiagramResponse generateDiagram(@Valid @RequestBody GenerateDiagramRequest request) {
    return diagramGenerationService.generate(request);
  }

  @PostMapping("/diagram/edit")
  public GenerateDiagramResponse editDiagram(@Valid @RequestBody UpdateMermaidRequest request) {
    return diagramGenerationService.editMermaid(request);
  }

  /**
   * 兼容入口：仅返回 Mermaid（后续前端统一改用 /api/ai/diagram）。
   */
  @PostMapping("/mermaid")
  public GenerateDiagramResponse generateMermaid(@Valid @RequestBody GenerateDiagramRequest request) {
    request.setLanguage("mermaid");
    return diagramGenerationService.generate(request);
  }

  @GetMapping("/demo")
  public String demo() {
    return "success";
  }
}
