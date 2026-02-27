# 仓库指南

## 项目结构与模块组织
- `frontend/` 为 Vue 2 前端代码，包含页面、Vuex、路由与 API 客户端（如 `frontend/src/api`）。
- `backend/` 为 Spring Boot 3 后端服务，包含 DTO、校验器与控制器（如 `backend/src/main/java`）。
- `frontend/public/drawio/` 存放集成的 diagrams.net 静态资源与插件。
- `doc/` 保存部署与使用说明（见 `doc/部署说明.md`）。

## 构建、测试与本地开发命令
- `cd backend && mvn spring-boot:run` 启动后端服务用于本地开发。
- `cd backend && mvn test` 运行后端单元/集成测试（Spring Boot 测试栈）。
- `cd frontend && npm install` 安装前端依赖。
- `cd frontend && npm run serve` 启动前端开发服务器。
- `cd frontend && npm run build` 构建生产包。
- `cd frontend && npm run lint` 执行 ESLint/Prettier 规范检查。

## 编码风格与命名规范
- Java：遵循 Spring Boot 约定，包名保持在 `com.easydraw.backend` 下。
- JS/Vue：2 空格缩进，组件文件使用 kebab-case（如 `EditorView.vue`），变量使用 camelCase。
- 规范工具：`frontend/` 已配置 ESLint + Prettier，提交前应保持通过。

## 测试指南
- 后端测试位于 `backend/src/test/java`，使用 Spring Boot 测试框架。
- 测试命名保持清晰（如 `*Test.java`），覆盖参数校验与响应结构。
- 前端未配置自动化测试，依赖手工 UI 回归与 lint 检查。

## 提交与合并请求规范
- 历史提交以简短中文描述为主，建议一条提交只聚焦一个改动。
- PR 需包含：变更说明、关联 Issue（如有）、UI 变更截图。
- 配置改动（如 `.env.local` 或 `application.yml`）需在 PR 中注明。

## 配置与安全提示
- 前端配置使用 `VUE_APP_DRAWIO_BASE_URL` 与 `VUE_APP_API_BASE_URL`（详见 `README.md`）。
- 模型凭证在浏览器 localStorage 中保存，公共环境避免使用真实密钥。
- 不要提交敏感信息，优先使用本地 `.env.local` 或环境变量。
