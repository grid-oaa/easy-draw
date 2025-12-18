# easy-draw frontend

技术栈：Vue 2 + JavaScript + vue-router@3 + vuex@3 + axios + Element UI。

## 功能说明（原型）

- 左侧：嵌入 `diagrams.net（draw.io）` 编辑器（iframe），负责渲染与编辑
- 右侧：AI 对话面板，用于输入“业务描述 + 图表类型”，生成 Mermaid 并导入到 draw.io 展示（后端接口实现后联通）

### 端到端流程（计划/约定）

1. 用户在右侧输入需求（例如：生成流程图 + 业务过程描述）
2. 前端调用后端生成接口，后端返回 Mermaid 语法文本
3. 前端将 Mermaid 导入到 draw.io 绘图区并展示，用户可继续编辑、导出

## 开发

```bash
npm install
npm run serve
```

### Windows PowerShell（ExecutionPolicy 导致 `npm` 失败）

如果遇到 `npm.ps1` 被禁止运行（ExecutionPolicy）：

```bash
npm.cmd install
npm.cmd run serve
```

或改用 `cmd` 运行：

```bash
cmd /c "npm run serve"
```

## 构建

```bash
npm run build
```
