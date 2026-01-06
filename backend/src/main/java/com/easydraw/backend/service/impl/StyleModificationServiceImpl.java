package com.easydraw.backend.service.impl;

import com.easydraw.backend.ai.AiClient;
import com.easydraw.backend.dto.ModifyStyleCommand;
import com.easydraw.backend.dto.StyleModifyRequest;
import com.easydraw.backend.service.StyleModificationService;
import com.easydraw.backend.style.ModifyStyleValidator;
import com.easydraw.backend.style.StylePromptTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class StyleModificationServiceImpl implements StyleModificationService {

  private final AiClient aiClient;
  private final StylePromptTemplate promptTemplate;
  private final ModifyStyleValidator validator;
  private final ObjectMapper objectMapper;

  public StyleModificationServiceImpl(
      AiClient aiClient,
      StylePromptTemplate promptTemplate,
      ModifyStyleValidator validator,
      ObjectMapper objectMapper) {
    this.aiClient = aiClient;
    this.promptTemplate = promptTemplate;
    this.validator = validator;
    this.objectMapper = objectMapper;
  }

  @Override
  public ModifyStyleCommand generate(StyleModifyRequest request) {
    // 将用户自然语言转为结构化样式指令提示词
    String systemPrompt = promptTemplate.render(request.getPrompt());
    String userPrompt = request.getPrompt();
    // 调用模型生成样式修改指令（JSON 文本）
    String raw =
        aiClient.generateWithSystemPrompt(systemPrompt, userPrompt, request.getModelConfig());

    // 解析并校验指令，避免无效或危险操作
    ModifyStyleCommand command = parseCommand(raw);
    validator.validate(command);
    return command;
  }

  private ModifyStyleCommand parseCommand(String raw) {
    String cleaned = stripCodeFence(raw);
    try {
      return objectMapper.readValue(cleaned, ModifyStyleCommand.class);
    } catch (Exception e) {
      throw new IllegalStateException("样式指令解析失败: " + e.getMessage(), e);
    }
  }

  private String stripCodeFence(String raw) {
    if (raw == null) return "";
    String trimmed = raw.trim();
    if (trimmed.startsWith("```")) {
      int idx = trimmed.indexOf('\n');
      if (idx > 0) {
        trimmed = trimmed.substring(idx + 1);
      }
      if (trimmed.endsWith("```")) {
        trimmed = trimmed.substring(0, trimmed.length() - 3);
      }
    }
    return trimmed.trim();
  }
}
