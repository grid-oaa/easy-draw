package com.easydraw.backend.diagram;

import java.util.Locale;

public enum DiagramLanguage {
  MERMAID("mermaid"),
  PLANTUML("plantuml");

  private final String code;

  DiagramLanguage(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static DiagramLanguage fromCode(String code) {
    if (code == null || code.trim().isEmpty()) return MERMAID;
    String normalized = code.trim().toLowerCase(Locale.ROOT);
    for (DiagramLanguage v : values()) {
      if (v.code.equals(normalized)) return v;
    }
    throw new IllegalArgumentException("Unsupported diagram language: " + code);
  }
}

