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
      errors.add(
          DiagramError.of("HEADER_MISSING", "缺少 Mermaid 图表头（例如 flowchart TD / sequenceDiagram）"));
    }

    if (countChar(trimmed, '[') != countChar(trimmed, ']')) {
      errors.add(DiagramError.of("BRACKET_MISMATCH", "方括号数量不匹配"));
    }

    if (countChar(trimmed, '(') != countChar(trimmed, ')')) {
      errors.add(DiagramError.of("PAREN_MISMATCH", "圆括号数量不匹配"));
    }

    if (diagramType != null && diagramType.toLowerCase().contains("flow")) {
      String lower = trimmed.toLowerCase();
      if (!lower.contains("flowchart") && !lower.contains("graph")) {
        errors.add(DiagramError.of("TYPE_MISMATCH", "diagramType expects flowchart or graph"));
      }
    }

    return errors;
  }

  private boolean looksLikeMermaidHeader(String text) {
    String[] lines = text.split("\\R");
    for (String raw : lines) {
      String line = raw.trim().toLowerCase();
      if (line.isEmpty() || line.equals("```") || line.equals("mermaid")) {
        continue;
      }
      if (line.startsWith("flowchart")
          || line.startsWith("graph")
          || line.startsWith("sequencediagram")
          || line.startsWith("statediagram")
          || line.startsWith("classdiagram")
          || line.startsWith("erdiagram")) {
        return true;
      }
      // 只检查第一条非空非 fence 行
      break;
    }

    // 兜底：在全文中找到任意合法头
    String lower = text.toLowerCase();
    return lower.contains("flowchart")
        || lower.contains("sequencediagram")
        || lower.contains("statediagram");
  }

  private int countChar(String text, char c) {
    int count = 0;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == c) count++;
    }
    return count;
  }
}