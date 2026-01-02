package com.easydraw.backend.style;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
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
    // 从 classpath 读取模板文件
    ClassPathResource resource = new ClassPathResource("STYLE_PROMPT");
    try (InputStream in = resource.getInputStream()) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
    } catch (IOException e) {
      throw new IllegalStateException("无法读取 STYLE_PROMPT(classpath)", e);
    }
  }
}