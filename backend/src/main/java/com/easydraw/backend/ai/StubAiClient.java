package com.easydraw.backend.ai;

import com.easydraw.backend.diagram.DiagramLanguage;
import org.springframework.stereotype.Component;

/**
 * 本地/无模型时的兜底实现。若配置了 bigmodel.api-key，会同时存在 Stub 与 BigModel，
 * 由于 BigModel 标注了 @Primary，默认会注入大模型；否则使用 Stub。
 */
@Component
public class StubAiClient implements AiClient {

  @Override
  public String generate(DiagramLanguage language, String diagramType, String prompt) {
    // MVP 占位：后续替换为真实模型调用（OpenAI/Bedrock/自建模型服务等）。
    if (language == DiagramLanguage.PLANTUML) {
      return """
          @startuml
          title Easy Draw (Stub)
          start
          :输入业务描述与图表类型;
          :生成 PlantUML;
          :校验 PlantUML;
          stop
          @enduml
          """;
    }

    // 默认 Mermaid
    if (diagramType != null && diagramType.toLowerCase().contains("sequence")) {
      return """
          sequenceDiagram
            participant U as 用户
            participant S as 系统
            U->>S: 提交需求
            S-->>U: 返回 Mermaid
          """;
    }

    return """
        flowchart TD
          A[开始] --> B[输入业务描述与图表类型]
          B --> C[生成 Mermaid]
          C --> D[校验 Mermaid]
          D -->|通过| E[返回 Mermaid 给前端]
          D -->|失败| F[提示修复/重试]
        """;
  }
}
