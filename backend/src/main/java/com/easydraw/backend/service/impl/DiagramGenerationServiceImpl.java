package com.easydraw.backend.service.impl;

import com.easydraw.backend.diagram.DiagramGenerationInput;
import com.easydraw.backend.diagram.DiagramLanguage;
import com.easydraw.backend.diagram.DiagramLanguageStrategy;
import com.easydraw.backend.dto.DiagramError;
import com.easydraw.backend.dto.GenerateDiagramRequest;
import com.easydraw.backend.dto.GenerateDiagramResponse;
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
