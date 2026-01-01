# Draw.io 图形样式修改能力文档

## 概述

Draw.io 现已支持通过 iframe postMessage API 接收样式修改指令，实现对图形元素的自动化样式修改。该功能允许外部系统（如 AI 助手、前端应用）通过发送 JSON 格式的指令来修改图形的各种视觉属性，无需用户手动操作。

## 核心能力

### 1. 目标选择能力

系统支持四种目标选择器，可以精确控制样式修改的范围：

| 目标选择器 | 描述 | 适用场景 |
|-----------|------|---------|
| `selected` | 当前选中的所有图形元素 | 用户已选择特定元素需要修改 |
| `edges` | 画布上所有的边（连接线） | 统一修改所有连接线样式 |
| `vertices` | 画布上所有的顶点（形状） | 统一修改所有形状样式 |
| `all` | 画布上所有的图形元素 | 应用全局样式主题 |

### 2. 样式修改能力

#### 2.1 颜色属性

支持修改以下颜色属性：

| 属性名 | 描述 | 示例值 |
|--------|------|--------|
| `fillColor` | 填充颜色 | `'#FF0000'`, `'red'`, `'none'` |
| `strokeColor` | 边框颜色 | `'#0000FF'`, `'blue'` |
| `fontColor` | 字体颜色 | `'#000000'`, `'black'` |
| `gradientColor` | 渐变颜色 | `'#00FF00'` |
| `labelBackgroundColor` | 标签背景颜色 | `'#FFFFFF'` |
| `labelBorderColor` | 标签边框颜色 | `'#CCCCCC'` |
| `shadowColor` | 阴影颜色 | `'#888888'` |
| `swimlaneFillColor` | 泳道填充颜色 | `'#E0E0E0'` |

**特性**：
- 支持十六进制颜色值（如 `#FF0000`）
- 支持颜色名称（如 `red`、`blue`）
- 支持 `none` 值表示无颜色
- 只支持绝对值设置，不支持相对操作

#### 2.2 数值属性

支持修改以下数值属性：

| 属性名 | 描述 | 有效范围 | 单位 |
|--------|------|---------|------|
| `strokeWidth` | 线条粗细 | ≥ 0 | 像素 |
| `fontSize` | 字体大小 | ≥ 1 | 点 |
| `opacity` | 整体透明度 | 0-100 | 百分比 |
| `fillOpacity` | 填充透明度 | 0-100 | 百分比 |
| `strokeOpacity` | 边框透明度 | 0-100 | 百分比 |
| `textOpacity` | 文字透明度 | 0-100 | 百分比 |
| `arcSize` | 圆角大小 | 0-100 | - |
| `rotation` | 旋转角度 | 0-360 | 度 |
| `spacing` | 内边距 | - | 像素 |
| `spacingTop` | 上内边距 | - | 像素 |
| `spacingBottom` | 下内边距 | - | 像素 |
| `spacingLeft` | 左内边距 | - | 像素 |
| `spacingRight` | 右内边距 | - | 像素 |

**特性**：
- 支持绝对值设置
- 支持相对操作（增加、减少、乘以）
- 自动限制在有效范围内

#### 2.3 开关属性

支持修改以下开关属性（值为 0 或 1）：

| 属性名 | 描述 | 0 表示 | 1 表示 |
|--------|------|--------|--------|
| `dashed` | 虚线样式 | 实线 | 虚线 |
| `rounded` | 圆角样式 | 直角 | 圆角 |
| `curved` | 曲线样式（仅边） | 直线 | 曲线 |
| `shadow` | 阴影效果 | 无阴影 | 有阴影 |
| `editable` | 是否可编辑 | 不可编辑 | 可编辑 |
| `movable` | 是否可移动 | 不可移动 | 可移动 |
| `resizable` | 是否可调整大小 | 不可调整 | 可调整 |
| `rotatable` | 是否可旋转 | 不可旋转 | 可旋转 |
| `deletable` | 是否可删除 | 不可删除 | 可删除 |

#### 2.4 枚举属性

支持修改以下枚举属性：

| 属性名 | 可选值 | 描述 |
|--------|--------|------|
| `align` | `left`, `center`, `right` | 水平对齐方式 |
| `verticalAlign` | `top`, `middle`, `bottom` | 垂直对齐方式 |
| `labelPosition` | `left`, `center`, `right` | 标签水平位置 |
| `verticalLabelPosition` | `top`, `middle`, `bottom` | 标签垂直位置 |

#### 2.5 箭头属性（仅边元素）

支持修改连接线的箭头样式：

| 属性名 | 类型 | 描述 |
|--------|------|------|
| `startArrow` | string | 起点箭头类型 |
| `endArrow` | string | 终点箭头类型 |
| `startSize` | number | 起点箭头大小 |
| `endSize` | number | 终点箭头大小 |
| `startFill` | 0 \| 1 | 起点箭头是否填充 |
| `endFill` | 0 \| 1 | 终点箭头是否填充 |

**箭头类型可选值**：
- `none` - 无箭头
- `classic` - 经典箭头
- `classicThin` - 细经典箭头
- `block` - 块状箭头
- `blockThin` - 细块状箭头
- `open` - 开放箭头
- `openThin` - 细开放箭头
- `oval` - 椭圆箭头
- `diamond` - 菱形箭头
- `diamondThin` - 细菱形箭头

**特性**：
- 箭头属性只对边元素有效
- 应用于非边元素时会被自动忽略

#### 2.6 字体属性

支持修改以下字体属性：

| 属性名 | 类型 | 描述 |
|--------|------|------|
| `fontFamily` | string | 字体名称（如 `Arial`, `Helvetica`） |
| `fontStyle` | number | 字体样式（位掩码：1=粗体, 2=斜体, 4=下划线） |

### 3. 操作模式

#### 3.1 绝对值设置

直接设置属性为指定的值。

**适用场景**：
- 设置特定颜色
- 设置固定的线条粗细
- 开启或关闭某个效果

**示例**：
```javascript
{
    fillColor: '#FF0000',    // 设置填充颜色为红色
    strokeWidth: 3,          // 设置线条粗细为 3 像素
    shadow: 1                // 开启阴影效果
}
```

#### 3.2 相对操作

在当前值的基础上进行修改，无需知道当前值。

**支持的操作类型**：

| 操作类型 | 描述 | 示例 | 结果 |
|---------|------|------|------|
| `set` | 设置为指定值 | 当前值 10，set 20 | 20 |
| `increase` | 增加指定值 | 当前值 10，increase 5 | 15 |
| `decrease` | 减少指定值 | 当前值 10，decrease 3 | 7 |
| `multiply` | 乘以指定倍数 | 当前值 10，multiply 2 | 20 |

**适用场景**：
- 加粗线条（不知道当前粗细）
- 放大字体（不知道当前大小）
- 调整透明度（不知道当前值）

**示例**：
```javascript
{
    strokeWidth: { op: 'increase', value: 2 },    // 线条加粗 2 像素
    fontSize: { op: 'multiply', value: 1.5 },     // 字体放大 1.5 倍
    opacity: { op: 'decrease', value: 20 }        // 透明度降低 20%
}
```

**限制**：
- 颜色属性只支持 `set` 操作
- 数值属性支持所有操作类型
- 操作结果会自动限制在有效范围内

### 4. 批量操作能力

支持在一次指令中同时修改多个属性：

**特性**：
- 所有修改在一次操作中完成
- 只触发一次视图更新，性能更好
- 支持原子撤销（一次撤销恢复所有属性）
- 可以混合使用绝对值和相对操作

**执行顺序**：
1. 先应用绝对值设置（`styles`）
2. 再应用相对操作（`operations`）

**示例**：
```javascript
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        fillColor: '#0000FF',
        strokeColor: '#FFFFFF',
        rounded: 1,
        shadow: 1
    },
    operations: {
        strokeWidth: { op: 'multiply', value: 2 },
        fontSize: { op: 'increase', value: 4 }
    }
}
```

### 5. 撤销和重做支持

所有样式修改操作都完全支持撤销和重做：

**特性**：
- 每次修改都会记录到撤销历史
- 批量修改作为一个操作记录
- 用户可以通过 Ctrl+Z 撤销修改
- 用户可以通过 Ctrl+Y 重做修改

## 使用场景示例

### 场景 1：修改选中元素颜色

```javascript
// 将选中的元素改为红色
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        fillColor: '#FF0000'
    }
}
```

### 场景 2：加粗所有连接线

```javascript
// 所有边的线条加粗 2 像素
{
    action: 'modifyStyle',
    target: 'edges',
    operations: {
        strokeWidth: { op: 'increase', value: 2 }
    }
}
```

### 场景 3：应用深色主题

```javascript
// 修改所有顶点为深色主题
{
    action: 'modifyStyle',
    target: 'vertices',
    styles: {
        fillColor: '#2C2C2C',
        strokeColor: '#FFFFFF',
        fontColor: '#FFFFFF'
    }
}

// 修改所有边为白色
{
    action: 'modifyStyle',
    target: 'edges',
    styles: {
        strokeColor: '#FFFFFF'
    }
}
```

### 场景 4：设置箭头样式

```javascript
// 为所有边添加经典箭头
{
    action: 'modifyStyle',
    target: 'edges',
    styles: {
        endArrow: 'classic',
        endSize: 10,
        endFill: 1
    }
}
```

### 场景 5：添加视觉效果

```javascript
// 为选中元素添加圆角和阴影
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        rounded: 1,
        shadow: 1,
        arcSize: 20
    }
}
```

### 场景 6：调整文字样式

```javascript
// 放大字体并居中对齐
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        align: 'center',
        verticalAlign: 'middle'
    },
    operations: {
        fontSize: { op: 'multiply', value: 1.5 }
    }
}
```

### 场景 7：设置虚线样式

```javascript
// 将所有边改为虚线
{
    action: 'modifyStyle',
    target: 'edges',
    styles: {
        dashed: 1
    }
}
```

### 场景 8：调整透明度

```javascript
// 降低所有元素的透明度
{
    action: 'modifyStyle',
    target: 'all',
    operations: {
        opacity: { op: 'decrease', value: 30 }
    }
}
```

## 智能特性

### 1. 自动值范围限制

系统会自动将属性值限制在有效范围内：

- **透明度属性**：自动限制在 0-100 之间
- **旋转角度**：自动限制在 0-360 之间
- **线条粗细**：自动限制为非负数
- **字体大小**：自动限制为至少 1

**示例**：
```javascript
// 即使设置超出范围的值，也会自动限制
{
    opacity: { op: 'set', value: 150 }  // 会被限制为 100
}
```

### 2. 智能属性过滤

系统会根据元素类型自动过滤不适用的属性：

- **箭头属性**：只应用于边元素，应用于顶点时会被忽略
- **曲线属性**：只应用于边元素

### 3. 错误容错

系统具有良好的错误容错能力：

- **无效属性**：忽略并继续处理其他属性
- **部分失败**：成功的属性会被应用，失败的会在响应中报告
- **空选择**：返回友好的警告信息

## 性能特性

### 1. 批量更新优化

- 使用 `model.beginUpdate()` 和 `model.endUpdate()` 包装所有修改
- 只触发一次视图更新，避免多次重绘
- 提高大量元素修改时的性能

### 2. 原子操作

- 所有修改作为一个原子操作执行
- 支持一次性撤销所有修改
- 保证数据一致性

## 安全特性

### 1. 输入验证

- 验证所有输入字段的类型和格式
- 验证属性值的有效范围
- 验证枚举值的有效性

### 2. 白名单机制

- 只处理预定义的属性名
- 只支持预定义的操作类型
- 不执行任何代码字符串

### 3. Origin 验证

- 支持配置允许的消息来源
- 拒绝来自未授权来源的消息

## 限制和约束

### 1. 颜色属性限制

- 颜色属性只支持绝对值设置
- 不支持 `increase`、`decrease`、`multiply` 操作
- 尝试对颜色使用相对操作会返回错误

### 2. 箭头属性限制

- 箭头属性只对边元素有效
- 应用于非边元素时会被自动忽略

### 3. 枚举值限制

- 枚举属性只接受预定义的值
- 无效的枚举值会返回错误

### 4. 目标选择限制

- `selected` 目标要求用户已选择元素
- 如果没有选中元素，会返回 `NO_TARGET_CELLS` 错误

## 响应和反馈

### 成功响应

```javascript
{
    event: 'modifyStyle',
    status: 'ok',
    data: {
        modifiedCount: 5  // 成功修改的元素数量
    }
}
```

### 部分成功响应

```javascript
{
    event: 'modifyStyle',
    status: 'ok',
    data: {
        modifiedCount: 5,
        failedCount: 2,
        errors: [
            { property: 'invalidProp', error: 'Unknown property' }
        ]
    }
}
```

### 错误响应

```javascript
{
    event: 'modifyStyle',
    status: 'error',
    error: 'Invalid target: unknown',
    errorCode: 'INVALID_TARGET'
}
```

### 错误代码

| 错误代码 | 描述 |
|---------|------|
| `INVALID_FORMAT` | 消息格式无效 |
| `INVALID_TARGET` | 无效的目标选择器 |
| `NO_TARGET_CELLS` | 没有目标元素 |
| `INVALID_PROPERTY` | 无效的属性名 |
| `INVALID_VALUE` | 无效的属性值 |
| `INVALID_OPERATION` | 无效的操作类型 |
| `UNSUPPORTED_OPERATION` | 不支持的操作（如对颜色使用相对操作） |
| `ORIGIN_DENIED` | 消息来源被拒绝 |

## 浏览器兼容性

### 支持的浏览器

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### 必需的浏览器 API

- `window.postMessage`
- `JSON.parse` / `JSON.stringify`

## 版本信息

- **功能版本**：1.0.0
- **最后更新**：2024-12
- **兼容性**：draw.io 版本 22.0.0+

## 相关文档

- [前端集成指南](./FRONTEND_INTEGRATION_GUIDE.md) - 如何在前端系统中使用该功能
- [API 文档](./STYLE_MODIFICATION_API.md) - 详细的 API 规范
- [样式参考](./DRAWIO_STYLE_REFERENCE.md) - 完整的样式属性参考
