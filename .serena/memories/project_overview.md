# 项目概览

## 项目目的
- Easy Draw：前端提供 draw.io 画布与 AI 面板，后端提供 AI 生成 Mermaid/PlantUML 的接口，并进行校验/清洗。

## 技术栈
- 后端：Spring Boot 3.x、Java 17、Maven、Web/WebFlux、Validation、Actuator。
- 前端：Vue 2、Vue Router、Vuex、Element UI、Axios、Vue CLI。

## 目录结构（粗略）
- backend/: Spring Boot 后端
  - src/main/java/com/easydraw/backend/ai: AI 适配层（AiClient 等）
  - src/main/java/com/easydraw/backend/service: 生成流程编排
  - src/main/java/com/easydraw/backend/mermaid|plantuml: 语法校验
  - src/main/resources: application.yml
- frontend/: Vue2 前端
  - src/components: AiPanel、CanvasStage 等

## 关键链路
- /api/ai/diagram -> DiagramGenerationService -> DiagramLanguageStrategy -> AiClient.generate -> 结果清洗与校验。