# easy-draw backend

## 技术栈

- Spring Boot 3
- Java 17

## 项目构建工具

- Maven 3.8.8

## 项目说明

本项目用于为 Easy Draw 前端提供后端接口能力，核心链路为：

1. 用户在前端 AI 聊天框输入“业务描述 + 要生成的图表类型”（如流程图）
2. 后端解析用户需求，明确图表类型、参与部门/步骤、关键连线关系等
3. 后端调用 AI 模型（可插拔）生成符合描述的 Mermaid 语法文本
4. 后端对 Mermaid 结果进行基础校验（必要时进行二次修复/重试）
5. 后端返回 Mermaid 文本给前端
6. 前端将 Mermaid 导入到 draw.io（diagrams.net）绘图区并展示，用户可继续编辑与导出

接口形态（MVP）：

- `POST /api/ai/diagram`
  - 入参：`language`（`mermaid`/`plantuml`，默认 `mermaid`）、`diagramType`、`prompt`
  - 出参：`content`（对应绘图语言文本）+ `validation`（校验结果）+ `explain`

## 业务模块划分

（MVP 建议拆分，后续可按需要扩展）

- API 接口层（Controller）
  - 接收前端生成请求：图表类型、业务描述、可选的上下文信息
  - 返回：Mermaid 文本 + 校验结果/错误信息 + 可选 explain（自然语言说明）
- 需求解析与编排（Service/Orchestrator）
  - 识别图表类型（flowchart/sequence/state 等）与生成偏好
  - 将用户描述结构化（步骤/角色/输入输出/分支等）
  - 生成 prompt（模板化），控制输出为 Mermaid（禁止输出多余内容）
- AI 模型适配层（AI Client）
  - 抽象统一接口（如 OpenAI/Bedrock/本地模型/HTTP 模型服务）
  - 支持超时、重试、降级与审计日志（MVP 可先简化）
- Mermaid 语法校验（Validator）
  - 基础校验：是否包含 Mermaid 关键字、括号/箭头等常见错误
  - 可选增强：接入 `mermaid-cli`（Node）或其它解析器做严格语法校验（可作为独立服务/容器）
  - 失败处理：自动修复（让模型只修复语法）或返回错误供前端提示
- 图表生成结果管理（可选）
  - 记录生成请求与结果（便于复用、回溯、指标统计）
  - 后续可接入版本管理/权限/存储（S3/DynamoDB/数据库等）

## 后端开发所需技术（建议）

### 1) Web 与工程化

- Spring Web（REST API）
- 参数校验：`spring-boot-starter-validation`
- 全局异常处理：统一错误码/错误信息结构（便于前端展示）
- API 文档：OpenAPI/Swagger（推荐 `springdoc-openapi`）
- 配置分环境：`application.yml` + `application-{profile}.yml`（dev/test/prod）

### 2) AI 调用与编排

- AI Client 抽象层（可插拔）
  - 统一接口：输入（图表类型 + 业务描述 + 约束）→ 输出（Mermaid 文本 + explain）
  - 超时、重试、限流、熔断（生产建议）
- Prompt 模板化与输出约束
  - 约束模型“只输出 Mermaid”，禁止多余解释（解释放到单独字段）
  - 约束语法类型（flowchart/sequence/state 等）与风格（节点命名、方向、换行等）
- 失败与修复策略
  - 生成后校验失败 → 进入“只修复语法/只修复最小改动”的二次调用
  - 必要时多轮重试并返回结构化错误（便于前端提示）

### 3) Mermaid 校验（重点）

- 基础校验（后端内置）
  - 必须包含 Mermaid 头（如 `flowchart TD` / `sequenceDiagram` 等）
  - 常见语法错误快速检测（括号不匹配、箭头/连线错误、非法字符等）
- 严格校验（可选增强）
  - 方案 A：独立校验服务（推荐），通过 HTTP 调用 `mermaid-cli` 或解析器进行严格校验
  - 方案 B：后端直接调用校验进程（需要 Node 运行时，部署复杂度更高）
- 校验失败处理
  - 输出：`valid=false + errors[]`（包含错误原因/位置/建议）
  - 自动修复：将 errors 与原 Mermaid 回传给模型，要求最小修复后再校验

### 4) 运维

- 日志
  - Logback（结构化日志建议 JSON）
  - TraceId/RequestId（链路追踪基础）
- 运行与部署（按需）
  - Docker 镜像构建（可选）
  - 反向代理/网关（Nginx / Spring Cloud Gateway，可选）
  - 配置与密钥管理（环境变量/配置中心，按需）
