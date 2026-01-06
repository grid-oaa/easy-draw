# Easy Draw

Easy Draw 是一个 AI + diagrams.net（draw.io）的在线绘图原型。
右侧用自然语言描述业务，AI 生成 Mermaid。
结果自动导入左侧 draw.io 画布继续编辑与导出。
![系统界面.png](doc/assets/%E7%B3%BB%E7%BB%9F%E7%95%8C%E9%9D%A2.png)

## 功能亮点
- 文本生成图表：自然语言生成 Mermaid 图表代码
- Mermaid 导入/插入：自动导入 draw.io 画布
- 图表增量修改：对已有 Mermaid 做调整/优化
- **样式修改能力：AI 指令 + 内置风格预设**
- 本地保存与导出：支持 .drawio / PNG / SVG
- 模型可配置：Base URL / API Key / Model


## 样式修改能力（核心特色）
本项目不止生成图表，还提供可控的样式修改能力。
用于统一风格与批量美化。

- AI 样式指令：自然语言生成结构化 JSON
- 内置风格预设：多套风格一键应用
- 作用范围可控：选中/全部/仅连线/仅节点
- 编辑流程融合：生成后继续修改样式

## 整体架构
- 前端：Vue 2 + Element UI + Vuex + axios
- 后端：Spring Boot 3 + Spring AI（OpenAI 兼容）
- 绘图引擎：diagrams.net iframe + mermaid-import 插件

## 目录结构
```
.
├─ frontend/   # 前端（Vue 2）
├─ backend/    # 后端（Spring Boot）
└─ doc/        # 部署与集成文档
```

## 快速开始（本地开发）
### 1) 准备 draw.io 资源（必需）
方式 A（本地开发）：
- 资源在 frontend/public/drawio
- 设置 VUE_APP_DRAWIO_BASE_URL=/drawio
方式 B（独立部署）：
- 部署 draw.io 静态站点并包含 plugins/mermaid-import.js
- 设置 VUE_APP_DRAWIO_BASE_URL=https://your-drawio.example.com
### 2) 启动后端
```
cd backend
mvn spring-boot:run
```
默认端口 8080，默认上下文路径为 /drawio。
### 3) 启动前端
```
cd frontend
npm install
npm run serve
```
如果后端上下文为 /drawio，请二选一：
- 方案 1：设置 VUE_APP_API_BASE_URL=http://localhost:8080/drawio/api
- 方案 2：修改 frontend/vue.config.js 代理到 http://localhost:8080/drawio
- 方案 3：启动后端时覆盖上下文路径为 /
### 4) 首次使用
- 右侧点击 Model 填写 Base URL / API Key / Model
- 输入描述生成图表

## API 说明
所有 AI 接口必须传 modelConfig（baseUrl/apiKey 必填）。
- POST /api/ai/diagram：生成 Mermaid/PlantUML
  - 参考请求：

```json
{
  "language": "mermaid",
  "diagramType": "",
  "prompt": "画一个订单支付流程",
  "modelConfig": {
    "baseUrl": "https://api.openai.com/v1",
    "apiKey": "sk-***",
    "model": "gpt-4o-mini"
  }
}
```

## 关键实现说明
- Mermaid 清洗：去除围栏与噪声，提升导入成功率
- Mermaid 插件：通过 postMessage 调用 mermaid-import
- 样式指令：/api/ai/style 返回结构化 JSON

## 配置项摘要
前端（.env.local 或环境变量）：
- VUE_APP_DRAWIO_BASE_URL：draw.io 地址（需支持 mermaid-import）
- VUE_APP_API_BASE_URL：后端地址
后端获取请求中的 modelConfig（baseUrl/apiKey/model，可选 provider/temperature/maxTokens）。

## 限制
- 依赖 mermaid-import 插件，官方 embed 可能无法导入
- 模型密钥保存在浏览器 LocalStorage，请勿在公共环境使用
