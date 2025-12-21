package com.easydraw.backend.service.impl;

import com.easydraw.backend.diagram.DiagramGenerationInput;
import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.diagram.DiagramLanguageStrategy;
import com.easydraw.backend.dto.DiagramError;
import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;
import com.easydraw.backend.dto.UpdateMermaidRequest;
import com.easydraw.backend.mermaid.MermaidSanitizer;
import com.easydraw.backend.service.DiagramGenerationService;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DiagramGenerationServiceImpl implements DiagramGenerationService {

  private final Map<DiagramLanguage, DiagramLanguageStrategy> strategyMap;

  public DiagramGenerationServiceImpl(List<DiagramLanguageStrategy> strategies) {
    Map<DiagramLanguage, DiagramLanguageStrategy> m = new EnumMap<>(DiagramLanguage.class);
    for (DiagramLanguageStrategy s : strategies) {
      m.put(s.language(), s);
    }
    this.strategyMap = m;
  }

  @Override
  public GenerateDiagramResponse generate(GenerateDiagramRequest request) {
    DiagramLanguage language = DiagramLanguage.fromCode(request.getLanguage());
    DiagramLanguageStrategy strategy = strategyMap.get(language);
    if (strategy == null) throw new IllegalStateException("No strategy for language: " + language);

    DiagramGenerationInput input =
        new DiagramGenerationInput(language, request.getDiagramType(), request.getPrompt());

    String content = stripCodeFence(strategy.generate(input));
    if (language == DiagramLanguage.MERMAID) {
      content = MermaidSanitizer.clean(content);
    }
    List<DiagramError> errors = strategy.validate(content, input);

    GenerateDiagramResponse.ValidationResult validation =
        errors.isEmpty()
            ? GenerateDiagramResponse.ValidationResult.ok()
            : GenerateDiagramResponse.ValidationResult.fail(errors);

    String explain = errors.isEmpty() ? "已生成" : "校验失败";

    return GenerateDiagramResponse.of(
        language.getCode(), request.getDiagramType(), content, validation, explain);
  }

  @Override
  public GenerateDiagramResponse editMermaid(UpdateMermaidRequest request) {
    DiagramLanguage language = DiagramLanguage.MERMAID;
    DiagramLanguageStrategy strategy = strategyMap.get(language);
    if (strategy == null) throw new IllegalStateException("No strategy for language: " + language);

    String existing = MermaidSanitizer.clean(request.getMermaid());
    String prompt = buildMermaidEditPrompt(existing, request.getPrompt());

    DiagramGenerationInput input = new DiagramGenerationInput(language, request.getDiagramType(), prompt);
    String content = stripCodeFence(strategy.generate(input));
    content = MermaidSanitizer.clean(content);
    List<DiagramError> errors = strategy.validate(content, input);

    GenerateDiagramResponse.ValidationResult validation =
        errors.isEmpty()
            ? GenerateDiagramResponse.ValidationResult.ok()
            : GenerateDiagramResponse.ValidationResult.fail(errors);

    String explain = errors.isEmpty() ? "已生成" : "校验失败";

    return GenerateDiagramResponse.of(
        language.getCode(), request.getDiagramType(), content, validation, explain);
  }

  private String stripCodeFence(String raw) {
    if (raw == null) return "";
    String trimmed = raw.trim();
    if (trimmed.startsWith("```")) {
      // remove leading fence
      int idx = trimmed.indexOf('\n');
      if (idx > 0) {
        trimmed = trimmed.substring(idx + 1);
      }
      // remove trailing fence
      if (trimmed.endsWith("```")) {
        trimmed = trimmed.substring(0, trimmed.length() - 3);
      }
    }
    return trimmed.trim();
  }

  private String buildMermaidEditPrompt(String existing, String request) {
    return String.join(
        "\n",
        "你是 Mermaid 图表编辑器。",
        "请根据用户的修改要求更新 Mermaid 图表。",
        "规则：",
        "- 仅输出 Mermaid 文本，不要解释或代码块。",
        "- 尽量保留已有节点 ID，不要给未变更节点重新编号。",
        "- 保持图表方向（graph/flowchart 头部方向）。",
        "- 如需新增节点，请使用新的唯一 ID（如 N10），并保持标签含义不变。",
        "- 只修改必要的部分，使用中文描述内容。",
        "",
        "当前 Mermaid：",
        existing,
        "",
        "用户请求：",
        request);
  }
}
