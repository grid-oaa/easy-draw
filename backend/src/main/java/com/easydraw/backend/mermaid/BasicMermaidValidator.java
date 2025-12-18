package com.easydraw.backend.mermaid;

import com.easydraw.backend.dto.DiagramError;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BasicMermaidValidator implements MermaidValidator {

  @Override
  public List<DiagramError> validate(String mermaid, String diagramType) {
    List<DiagramError> errors = new ArrayList<>();

    if (mermaid == null || mermaid.trim().isEmpty()) {
      errors.add(DiagramError.of("EMPTY", "Mermaid 内容为空"));
      return errors;
    }

    String trimmed = mermaid.trim();
    if (!looksLikeMermaidHeader(trimmed)) {
      errors.add(DiagramError.of("HEADER_MISSING", "缺少 Mermaid 图表头（例如 flowchart TD / sequenceDiagram）"));
    }

    if (countChar(trimmed, '[') != countChar(trimmed, ']')) {
      errors.add(DiagramError.of("BRACKET_MISMATCH", "方括号数量不匹配"));
    }

    if (countChar(trimmed, '(') != countChar(trimmed, ')')) {
      errors.add(DiagramError.of("PAREN_MISMATCH", "圆括号数量不匹配"));
    }

    if (diagramType != null && diagramType.toLowerCase().contains("flow") && !trimmed.contains("flowchart")) {
      errors.add(DiagramError.of("TYPE_MISMATCH", "diagramType 期望为 flowchart，但未检测到 flowchart 头"));
    }

    return errors;
  }

  private boolean looksLikeMermaidHeader(String text) {
    String firstLine = text.split("\\R", 2)[0].trim().toLowerCase();
    return firstLine.startsWith("flowchart")
        || firstLine.startsWith("sequencediagram")
        || firstLine.startsWith("statediagram")
        || firstLine.startsWith("classdiagram")
        || firstLine.startsWith("erdiagram");
  }

  private int countChar(String text, char c) {
    int count = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == c) count++;
    }
    return count;
  }
}

