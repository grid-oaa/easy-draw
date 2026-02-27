# AGENTS.md（项目级）
> 本文件用于指导在**本仓库**中工作的 coding agent。  
> 目标：明确如何**安装、运行、测试**，并提供安全修改的**护栏规则**。

---

## 1）项目概览
- **项目用途：** Easy Draw 是一个 AI + draw.io 在线绘图项目，用户通过自然语言生成或修改图表，后端调用大模型输出 Mermaid，前端将结果导入 diagrams.net 进行可视化编辑与导出。
- **主要技术栈：** Java 17 / Spring Boot 3.3.5 / Spring AI / Maven / Vue 2 / Vuex / Vue Router / Element UI / axios / npm
- **关键目录说明：**
  - `backend/src/` — 后端源码（Controller/Service/配置与测试）
  - `frontend/src/` — 前端源码（页面、组件、路由、状态管理）
  - `frontend/public/drawio/` — 内置 draw.io 静态资源
  - `doc/` — 项目文档与演示素材
  - `.github/workflows/` — CI/CD 工作流配置

---

## 2）护栏规则（强约束）
### 2.1 限定范围（Scoped work）
仅处理用户明确提出的目标，不主动扩展到重构、依赖升级、目录重排或“顺手优化”。若需求不清晰（例如要改后端、前端或联调），先提问确认再改。

### 2.2 修改范围（Change scope）
默认做最小可验证改动：只改与当前需求直接相关的文件与逻辑，不改无关命名、注释、格式或行为。涉及多文件时先列出“将修改哪些文件、为什么修改”。修改代码后必须生成<修改内容主题>，并在本文件“代码变更记录”章节追加记录。

### 2.3 最简原则（Minimalism first）
优先复用现有实现与工具方法，优先局部补丁，不新增同类依赖；遵循“无改动 > 小改动 > 大改动”。除非用户明确要求，否则不修改数据库结构、不新增框架、不做跨层改造。

### 2.4 必须提问（AskUserQuestion）
以下场景必须先问再做：需求验收标准不明确；目标文件不明确；存在多种方案且取舍不同；可能破坏兼容/接口/数据；批量替换、删除、重命名、升级等高风险操作。提问格式使用：
- Goal：（A 修复问题 / B 新增功能 / C 重构优化）
- Scope：（A 单文件 / B 单模块 / C 跨模块）
- Permission：（A 允许改接口 / B 允许移动文件 / C 允许新增依赖）→（Yes/No）

---

## 3）环境安装与运行命令
> 命令必须准确且可复制粘贴。

### 安装依赖
在仓库根目录执行：
```powershell
# 后端（Maven 会自动拉取依赖并编译）
cd backend
mvn clean compile

# 前端
cd ..\frontend
npm install
# 若 PowerShell 执行策略拦截，可使用：
npm.cmd install
```

### 启动开发环境（dev）
在两个终端分别执行：
```powershell
# 终端1：后端（默认 http://127.0.0.1:8081/drawio）
cd backend
mvn spring-boot:run

# 终端2：前端（默认 http://localhost:8080）
cd frontend
npm run serve
# PowerShell 受限时可用：
npm.cmd run serve
```

---

## 4）测试说明
### 运行测试
```powershell
# 后端单元测试
cd backend
mvn test

# 前端当前未配置独立单元测试命令，可先执行 lint 作为基础质量检查
cd ..\frontend
npm run lint
```

### 格式化 / 类型检查
```powershell
# 前端代码规范检查（ESLint）
cd frontend
npm run lint

# 本仓库未配置独立 TypeScript 类型检查命令（Vue 2 + JS）
# 候选占位：<command>
# 示例候选：npm run lint
```

---

## 5）构建与 CI 说明（可选）
本地构建命令：
```powershell
# 后端打包
cd backend
mvn clean package -DskipTests

# 前端构建
cd ..\frontend
npm run build
```
CI 路径：`.github/workflows/main.yml`。当前 CI 在 `main` 分支 push 时执行 Docker 镜像构建并推送 `backend`，随后通过 SSH 调用服务器 `/project/easy-draw/deploy.sh` 部署；与本地差异是 CI 使用 Ubuntu + Docker Buildx + GHCR，不直接执行前端构建命令。

---

## 6）安全注意事项
- 严禁将 API Key、Token、SSH 私钥写入代码仓库；前端模型密钥仅允许保存在浏览器本地存储。
- 提交前检查日志与配置，避免输出敏感信息（请求头、密钥、生产地址、个人数据）。
- 不要修改生产环境关键配置（如服务端口、上下文路径、网关地址）除非用户明确授权。
- `.env*`、`application*.yml` 变更需说明影响环境与回滚方式。
- 调试第三方模型接口时，优先使用脱敏日志并控制超时/重试，避免泄漏与雪崩调用。

---

## 7）代码变更记录
> 每次完成一次代码修改后，必须在本章节追加记录，便于追踪、review。

### YYYY-MM-DD <变更标题（简短）>
- **目标/背景：**
- **改动范围：**
- **修改内容摘要：**
- **验证方式：**
- **风险与回滚：**
- **备注：**（可选）

### 2026-02-27 AGENTS项目级文档填充
- **目标/背景：** 按指定模板生成本仓库项目级 AGENTS.md，补齐可执行命令与护栏规则。
- **改动范围：** `AGENTS.md`（整文件替换，占位内容改为可执行说明）。
- **修改内容摘要：** 完成项目概览、护栏规则、安装运行、测试、构建CI、安全注意事项，并保留模板要求的记录占位节。
- **验证方式：** 基于 `README.md`、`backend/pom.xml`、`frontend/package.json`、`.github/workflows/main.yml`、环境变量文件核对命令与配置。
- **风险与回滚：** 风险低（仅文档改动）；回滚可通过 Git 恢复 `AGENTS.md` 到上一版本。
- **备注：** 无。

---
