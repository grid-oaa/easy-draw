package com.easydraw.backend.style;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class StylePromptTemplate {

  private final String template;

  public StylePromptTemplate() {
    this.template = loadTemplate();
  }

  public String render(String userInput) {
    if (template.contains("{user_input}")) {
      return template.replace("{user_input}", userInput);
    }
    return template + System.lineSeparator() + "用户指令: " + userInput;
  }

  private String loadTemplate() {
    // 从 doc/STYLE_PROMPT 读取样式修改提示词
    Path path = Path.of("doc", "STYLE_PROMPT");
    try {
      return Files.readString(path, StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      throw new IllegalStateException("无法读取 STYLE_PROMPT: " + path.toAbsolutePath(), e);
    }
  }
}