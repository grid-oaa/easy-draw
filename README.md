# Easy Draw 项目总结

## 功能清单
- AI 聊天生成流程图：在聊天框输入业务描述，生成 Mermaid 并渲染到左侧 draw.io 画布。
- Mermaid 持续编辑：当已有图表时，支持“修改/调整/优化”等指令进行增量编辑。
- draw.io 嵌入与交互：支持导入 Mermaid、插入 Mermaid 到画布空白区域、导出/保存 drawio 与 PNG。
- 消息持久化：聊天记录与最近 Mermaid 结果保存到 LocalStorage，刷新不丢。
- UI 交互优化：聊天区可收起、复制/插入按钮、清空对话与画布、顶部精简。
- 用户模型配置：前端允许用户输入 baseUrl/apiKey/model，后端按请求动态调用对应模型。

## 关键实现细节
- 后端 AI 调用：统一使用 `BigModelAiClient` 通过 WebClient 调 OpenAI 兼容协议（chat/completions）。
- 动态模型配置：`GenerateDiagramRequest`/`UpdateMermaidRequest` 增加 `modelConfig`，每次请求传入后端，按需构建 WebClient。
- Mermaid 清洗与校验：`MermaidSanitizer` 负责去围栏、规范 header，避免 draw.io 导入失败。
- draw.io 导入：使用本地 draw.io + 自定义插件 `mermaid-import.js`，通过 postMessage 触发导入与插入，并监听 ACK。
- 本地 draw.io 部署：使用 `npx serve` 直接提供 draw.io 源码目录，前端通过 `.env.local` 指定嵌入地址。

## 项目难点
- draw.io 嵌入与导入限制：官方 embed 不支持 import，需要自定义插件与消息协议。
- Mermaid 兼容问题：模型生成的语法可能不符合 draw.io 解析规则，需要清洗/规范化。
- 编辑流程一致性：增量修改要求保留原 ID 与方向，避免图形结构被重排。
- 本地部署路径差异：本地 draw.io 的目录结构/路径需要与 iframe URL 对齐。

## 部署与启动流程
### 1. 本地 draw.io（前端嵌入）
- 解压 draw.io 包到本机目录，例如 `D:\\dev\\drawio-29.2.9`
- 启动静态服务（示例）
  - `npx serve -l 8085`
- 前端配置 `.env.local`
  - `VUE_APP_DRAWIO_BASE_URL=http://localhost:8085/src/main/webapp`

### 2. 后端
- 运行 Spring Boot 后端
- API 入口
  - 生成：`POST /api/ai/diagram`
  - 编辑：`POST /api/ai/diagram/edit`

### 3. 前端
- 安装依赖并启动
  - `npm install`
  - `npm run serve`
- 首次使用需在界面中填写模型配置（baseUrl/apiKey/model）

## 目录与关键文件
- 后端
  - `backend/src/main/java/com/easydraw/backend/ai/BigModelAiClient.java`
  - `backend/src/main/java/com/easydraw/backend/service/impl/DiagramGenerationServiceImpl.java`
  - `backend/src/main/java/com/easydraw/backend/mermaid/MermaidSanitizer.java`
- 前端
  - `frontend/src/components/AiPanel.vue`
  - `frontend/src/components/CanvasStage.vue`
  - `frontend/src/layouts/EditorLayout.vue`
  - `frontend/.env.local`

