# Spring AI 对接方案（Easy Draw 后端）

## 1. 前置分析

### 1.1 项目结构与技术栈
- 技术栈：Spring Boot 3.3.5 + Web/WebFlux + Validation + Actuator。
- 后端目录：`backend` 为主服务，`ai` 包含 `AiClient` 抽象与 `BigModelAiClient`/`StubAiClient` 实现。
- 调用路径：`AiController` -> `DiagramGenerationService` -> `DiagramLanguageStrategy` -> `AiClient.generate`。

### 1.2 业务目标与核心流程
- 目标：根据用户描述生成 Mermaid/PlantUML 图表文本，并进行语法校验后返回。
- 核心流程：前端发起 `/api/ai/diagram` -> 构造提示词 -> 调用大模型 -> 清洗/校验 -> 返回图表文本。

### 1.3 关键模块与高风险点
- `AiClient`：是 AI 访问的唯一入口，影响所有图表生成路径。
- `ModelConfig`：允许请求级传入模型参数，存在安全与稳定性风险。
- 风险点：超时、模型返回非结构化文本、不同厂商参数差异、密钥管理与审计。

### 1.4 基于项目特点的最佳实践建议
- 保持 `AiClient` 作为唯一接入层，避免各策略直接依赖厂商 SDK。
- 将厂商差异收敛为“路由 + 选项”模型，统一错误与指标埋点。
- 生产环境禁止直接透传 API Key，建议白名单或服务端配置优先。
- 先支持“OpenAI 兼容接口”，再按业务需要扩展厂商专属能力。

## 2. 设计目标
- 用 Spring AI 统一接入多个厂商（OpenAI 兼容、Azure OpenAI、Bedrock 等）。
- 保持现有接口与业务流程稳定，优先做到“无感切换”。
- 支持请求级选择模型与少量推理参数，同时保留服务端默认配置。
- 提供可回滚方案（保留 Stub/旧实现）。

## 3. 总体方案
- 引入 Spring AI 作为“统一模型适配层”，新增 `SpringAiClient` 替代 `BigModelAiClient`。
- 在 `AiClient` 内部实现“模型路由器”，按 `provider` 或默认配置选择 `ChatModel`。
- 复用现有提示词构造逻辑与校验流程，尽量降低业务改动面。
- 对接分两阶段：
  1) OpenAI 兼容接口（覆盖大多数厂商）
  2) 逐步接入厂商原生 Starter（如 Azure OpenAI、Bedrock 等）

## 4. 组件与配置设计

### 4.1 依赖与版本
建议通过 BOM 统一管理 Spring AI 版本，并注意：若使用里程碑版本（如 `1.0.0-M6`），需要配置 Spring Milestone 仓库。

```xml
<properties>
  <spring-ai.version>1.0.0-M6</spring-ai.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-bom</artifactId>
      <version>${spring-ai.version}</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<repositories>
  <repository>
    <id>spring-milestones</id>
    <name>Spring Milestones</name>
    <url>https://repo.spring.io/milestone</url>
  </repository>
</repositories>

<dependencies>
  <!-- OpenAI 兼容接口（用于对接 BigModel 的 OpenAI 协议端点） -->
  <dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
  </dependency>
</dependencies>
```

> 注意：如果你的 Maven `settings.xml` 配置了 `mirrorOf=*`（例如 central-only 镜像），会拦截 `repo.spring.io`，导致依赖无法下载；需要将 mirrorOf 调整为仅镜像 central。

### 4.2 配置结构（示例）
以下为示例配置，实际键名请以 Spring AI 官方文档为准：

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: ${OPENAI_BASE_URL} # OpenAI 兼容接口
      chat:
        options:
          model: ${OPENAI_MODEL:glm-4.6v-flash}
          temperature: 0.2
          max-tokens: 2048

# 业务侧默认模型配置（可选）
easy-draw:
  ai:
    provider: openai-compatible
    model: glm-4.6v-flash
```

### 4.3 SpringAiClient 设计（核心）
- 不依赖 Spring Boot 自动装配的 `OpenAiChatModel/ChatClient`，避免启动时强制要求服务端配置 key。
- 每次请求根据 `ModelConfig.baseUrl/apiKey` 创建 `OpenAiApi/OpenAiChatModel`，实现“用户自带 key”。
- `ModelConfig` 中的 `model/temperature/maxTokens` 转为 `OpenAiChatOptions`。

示意代码（方法名需与 Spring AI 版本对齐）：

```java
@Component
public class SpringAiClient implements AiClient {

  private final AiModelRouter router;

  public SpringAiClient(AiModelRouter router) {
    this.router = router;
  }

  @Override
  public String generate(DiagramLanguage language, String diagramType, String prompt, ModelConfig modelConfig) {
    OpenAiApi api = new OpenAiApi(modelConfig.getBaseUrl(), modelConfig.getApiKey());
    OpenAiChatModel chatModel = new OpenAiChatModel(api);
    OpenAiChatOptions options = router.toOptions(modelConfig);

    Prompt chatPrompt = new Prompt(
        List.of(new SystemMessage(systemPrompt(language)), new UserMessage(prompt)),
        options
    );

    return chatModel.call(chatPrompt).getResult().getOutput().getContent().trim();
  }
}
```

> 需要在 `application.yml` 中排除自动装配：`OpenAiAutoConfiguration`、`ChatClientAutoConfiguration`，避免启动时校验服务端 key。

### 4.4 模型路由与参数收敛
建议新增 `AiModelRouter`：
- 输入：`ModelConfig`（增加 `provider`、`temperature`、`maxTokens` 等字段）
- 输出：`ChatModel` 选择结果 + `ChatOptions`
- 规则：
  - 请求未指定 `provider` 时，使用应用默认配置
  - 仅允许少量安全参数透传，API Key 只允许服务端配置
  - 需记录 provider/model 用于日志与审计

### 4.5 兼容与回滚
- 保留 `StubAiClient` 作为无模型环境兜底。
- `SpringAiClient` 与 `BigModelAiClient` 通过 `@ConditionalOnProperty` 进行切换，确保可快速回滚。

## 5. 实施步骤（建议 6-8 步）
1. 引入 Spring AI BOM 与所需 Starter（先 OpenAI 兼容接口）。
2. 新增 `SpringAiClient` 与 `AiModelRouter`，保持 `AiClient` 接口不变。
3. 扩展 `ModelConfig`（可选字段：`provider`/`temperature`/`maxTokens`），并在 Controller 校验。
4. 增加 `application.yml` 的 Spring AI 配置与默认模型配置。
5. 迁移提示词构造逻辑至通用方法，保证行为一致。
6. 增加异常封装与日志结构化输出（provider/model/requestId）。
7. 编写集成测试：mock `ChatModel` 或使用 Spring AI Test 组件。
8. 灰度发布：通过配置切换到 `SpringAiClient`，保留回滚通道。

## 6. 测试与验收
- 单元测试：`AiModelRouter` 的 provider 选择与参数收敛。
- 集成测试：`/api/ai/diagram` 在不同 provider 下的响应格式与校验结果一致。
- 压测/稳定性：验证超时、重试、异常传播路径。

## 7. 风险与缓解
- 配置兼容风险：Spring AI 版本差异导致属性名变化。
  - 缓解：锁定版本，升级前进行配置回归验证。
- 请求级透传 API Key 风险：
  - 缓解：生产环境禁止透传，仅允许服务端配置。
- 不同模型返回风格差异：
  - 缓解：加强提示词约束与语法校验。
