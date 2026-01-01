# 样式修改对话能力方案与任务拆分

## 背景与目标
当前系统已支持“对话生成图形”（Mermaid 生成 → draw.io 渲染）。新需求是：用户用自然语言提出样式修改（如“将选中部分的线条加粗”），后端 AI 返回规定 JSON 格式，前端将该 JSON 通过 `postMessage` 发送给 `mermaid-import.js`，由插件解析并修改图形。

关键约束
- “生成图形”和“修改图形”的系统 prompt 必须分离。
- 样式修改 JSON 的格式必须符合 /doc 中的规范。

## 现状梳理（基于仓库与文档）
- 前端：已有 AI 对话生成 Mermaid 的链路。
- 插件：`mermaid-import.js` 已支持 `modifyStyle` 动作（参见文档中的 JSON 协议）。
- 文档：`doc/STYLE_MODIFICATION_API.md` 与 `doc/STYLE_MODIFICATION_INTEGRATION_GUIDE.md` 已定义 JSON 结构与示例。

## 总体方案（系统视角）
1. 意图识别：区分用户是“生成图形”还是“修改样式”。
2. 不同 AI 路径：
   - 生成图形：走现有 `/api/ai/diagram`（Mermaid 生成 prompt）。
   - 修改样式：新增 `/api/ai/style`（样式修改 prompt → JSON 输出）。
3. 前端执行：收到样式 JSON 后通过 `postMessage` 发送到 iframe，触发 `modifyStyle`。
4. 回执处理：监听 `modifyStyle` 响应，给用户明确成功/失败反馈。

## 关键模块设计

### 1) 前端（对话分流 + 执行修改）
- 新增指令识别策略（优先级从高到低）：
  1. 关键词启发式（如“加粗/改颜色/圆角/阴影/透明/字体/选中/线条/边框”等）。
  2. 显式模式开关：“生成图形 / 修改样式”切换。
  3. 后端分类接口（可选）：由 AI 判断当前意图。
- 调用修改接口：当判定为样式修改时调用 `/api/ai/style`，获取 JSON。
- 前端校验：在发送给 iframe 前做一次 JSON 合规校验（action/target/styles/operations）。
- postMessage：发送 `modifyStyle` JSON 给 iframe。
- 回执处理：监听 `modifyStyle` 事件并提示用户成功/失败原因。

### 2) 后端（样式修改新接口）
- 新增接口：`POST /api/ai/style`（建议命名，可统一为 `/api/ai/diagram/style`）。
- 输入：`prompt`（用户原始指令）、可选上下文（如 target 默认值、最近操作）。
- 输出：严格 JSON，仅包含 `ModifyStyleRequest` 字段。
- 校验与规范化：
  - 必须 `action=modifyStyle`
  - `target ∈ {selected, edges, vertices, all}`
  - 至少包含 `styles` 或 `operations`
  - 颜色属性仅允许绝对值（参考文档）。
- 错误处理：
  - AI 输出非法 JSON → 二次纠正（带“只输出 JSON”的约束 prompt）。
  - 校验失败 → 返回结构化错误给前端。

### 3) AI Prompt 设计（核心差异）
生成图形 prompt（已有）：
目标是生成 Mermaid 语法，并输出 `content`、`explain` 等。

样式修改 prompt（新增）：
目标是生成严格 JSON，且仅包含“样式修改指令”。
要求：
- 只能输出 JSON，不要解释。
- 严格遵循 `ModifyStyleRequest` 结构（见 /doc/STYLE_MODIFICATION_API.md）。
- 颜色属性使用 `styles`；数值改动优先用 `operations`。

参考文档：
- `doc/STYLE_MODIFICATION_API.md`
- `doc/DRAWIO_STYLE_REFERENCE.md`
- `doc/DRAWIO_STYLE_CAPABILITIES.md`

## 数据流（简化）
```
用户指令 -> 前端意图识别 -> (生成/修改分流)
修改分流 -> /api/ai/style -> JSON
JSON 校验 -> postMessage(modifyStyle)
iframe 返回 -> 前端提示
```

## 风险与对策
- 误判意图：建议加“模式切换”或后端分类接口。
- JSON 不合规：后端做严格校验 + 二次修正 prompt。
- 用户未选中元素：`NO_TARGET_CELLS` 回执提示引导用户先选择。
- 非法属性或值：前端/后端双重校验。

## 任务拆分（可执行）

### 阶段 A：后端能力补齐
1. 新增 DTO：`ModifyStyleRequest`（严格字段与枚举）。
2. 新增接口：`POST /api/ai/style`。
3. 新增 Prompt 模板：样式修改专用系统提示词。
4. 输出 JSON 校验器：校验 action/target/styles/operations 结构。
5. 错误码与消息规范化（与前端一致）。

### 阶段 B：前端集成
1. 新增对话意图分流逻辑（模式切换或关键词识别）。
2. 调用 `/api/ai/style` 并解析 JSON。
3. 发送 `modifyStyle` 到 iframe。
4. 监听回执并展示修改结果与错误提示。
5. 可选：失败自动降级为“生成图形”。

### 阶段 C：测试与文档
1. 接口联调用例：
   - “将选中线条加粗”
   - “把所有节点改为蓝色”
   - “把字体增大”
2. 异常场景：未选中元素、非法属性、JSON 结构错误。
3. 更新 `backend/README.md` 与前端说明（新增接口与使用示例）。

## 里程碑与验收
- M1：后端样式接口可用（可返回合法 JSON）。
- M2：前端可触发修改并收到回执。
- M3：主流程稳定（3 类指令 + 2 类错误均可反馈）。

## 附录：接口示例
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "operations": {
    "strokeWidth": { "op": "increase", "value": 2 }
  }
}
```