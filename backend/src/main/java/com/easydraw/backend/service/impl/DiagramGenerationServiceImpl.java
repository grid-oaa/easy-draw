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


  /**
   * 生成图表主流程：选择语言策略 -> 调用模型生成 -> 清洗与校验 -> 封装响应。
   *
   * @param request 生成请求
   * @return 生成结果
   */
  @Override
  public GenerateDiagramResponse generate(GenerateDiagramRequest request) {
    // 判断是哪种绘图语言
    DiagramLanguage language = DiagramLanguage.fromCode(request.getLanguage());
    // 根据请求语言选择对应策略，避免混用渲染语法
    DiagramLanguageStrategy strategy = strategyMap.get(language);
    if (strategy == null) throw new IllegalStateException("No strategy for language: " + language);

    DiagramGenerationInput input =
        new DiagramGenerationInput(
            language, request.getDiagramType(), request.getPrompt(), request.getModelConfig());

    // 调用模型生成图表文本，并去除围栏代码块
    String content = stripCodeFence(strategy.generate(input));
    if (language == DiagramLanguage.MERMAID) {
      // Mermaid 需要清洗以提升 draw.io 导入成功率
      content = MermaidSanitizer.clean(content);
    }
    // 语法校验与错误收集，前端可据此提示
    List<DiagramError> errors = strategy.validate(content, input);

    GenerateDiagramResponse.ValidationResult validation =
        errors.isEmpty()
            ? GenerateDiagramResponse.ValidationResult.ok()
            : GenerateDiagramResponse.ValidationResult.fail(errors);

    String explain = errors.isEmpty() ? "已生成" : "校验失败";

    return GenerateDiagramResponse.of(
        language.getCode(), request.getDiagramType(), content, validation, explain);
  }

  /**
   * 去除```mermaid```部分
   * @param raw
   * @return
   */
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
}
