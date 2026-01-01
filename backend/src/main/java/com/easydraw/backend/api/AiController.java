package com.easydraw.backend.api;

import com.easydraw.backend.ai.AiClient;
import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;
import com.easydraw.backend.dto.ModelTestRequest;
import com.easydraw.backend.dto.ModelTestResponse;
import com.easydraw.backend.dto.ModifyStyleCommand;
import com.easydraw.backend.dto.StyleModifyRequest;
import com.easydraw.backend.dto.UpdateMermaidRequest;
import com.easydraw.backend.service.DiagramGenerationService;
import com.easydraw.backend.service.StyleModificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

  private final DiagramGenerationService diagramGenerationService;
  private final StyleModificationService styleModificationService;
  private final AiClient aiClient;

  public AiController(
      DiagramGenerationService diagramGenerationService,
      StyleModificationService styleModificationService,
      AiClient aiClient) {
    this.diagramGenerationService = diagramGenerationService;
    this.styleModificationService = styleModificationService;
    this.aiClient = aiClient;
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

  @PostMapping("/style")
  public ModifyStyleCommand modifyStyle(@Valid @RequestBody StyleModifyRequest request) {
    return styleModificationService.generate(request);
  }

  @PostMapping("/model/test")
  public ModelTestResponse testModel(@Valid @RequestBody ModelTestRequest request) {
    String prompt = request.getPrompt();
    if (prompt == null || prompt.isBlank()) {
      prompt = "请回复 OK";
    }
    try {
      String output =
          aiClient.generateWithSystemPrompt(
              "你是连通性测试助手，只返回简短结果。", prompt, request.getModelConfig());
      return new ModelTestResponse(true, "模型连接正常", output);
    } catch (Exception e) {
      return new ModelTestResponse(false, e.getMessage(), null);
    }
  }

  @GetMapping("/demo")
  public String demo() {
    return "success";
  }
}
