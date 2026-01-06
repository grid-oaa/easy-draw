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

  /**
   * 1. 校验请求参数（prompt 必填，language 可选）。
   * 2. 交给 DiagramGenerationService 进行语言路由、模型生成与内容清洗。
   * 3. 返回生成内容与校验结果。
   *
   * @param request 生成请求（语言、图类型、提示词、模型配置）
   * @return 生成结果（内容、校验状态与说明）
   */
  @PostMapping("/diagram")
  public GenerateDiagramResponse generateDiagram(@Valid @RequestBody GenerateDiagramRequest request) {
    return diagramGenerationService.generate(request);
  }

  /**
   * 样式修改
   * @param request
   * @return
   */
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
}
