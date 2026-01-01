# 样式修改 iframe 集成 API 文档

## 概述

本文档描述了 draw.io 的样式修改 iframe 集成功能的 API 规范。该功能允许外部前端系统（如 AI 助手）通过浏览器的 `postMessage` API 向嵌入的 draw.io iframe 发送样式修改指令，draw.io 将自动解析并应用到指定的图形元素上。

## 快速开始

```javascript
// 获取 iframe 引用
const iframe = document.getElementById('drawio-iframe');

// 修改选中元素的填充颜色
iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        fillColor: '#FF0000',
        strokeWidth: 3
    }
}), '*');

// 监听响应
window.addEventListener('message', function(evt) {
    const data = JSON.parse(evt.data);
    if (data.event === 'modifyStyle') {
        if (data.status === 'ok') {
            console.log('操作成功！修改了', data.data.modifiedCount, '个元素');
        } else {
            console.error('错误：', data.error);
        }
    }
});
```

## 消息格式

### 请求消息

#### 基本格式

```typescript
interface ModifyStyleRequest {
    action: 'modifyStyle';           // 必需：操作类型
    target: 'selected' | 'edges' | 'vertices' | 'all';  // 必需：目标选择器
    styles?: {                       // 可选：绝对样式值
        [key: string]: string | number;
    };
    operations?: {                   // 可选：相对操作
        [key: string]: {
            op: 'set' | 'increase' | 'decrease' | 'multiply';
            value: number;
        };
    };
}
```

#### 字段说明

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `action` | string | 是 | 必须为 `'modifyStyle'` |
| `target` | string | 是 | 目标选择器，指定要修改的元素类型 |
| `styles` | object | 否* | 绝对样式值设置（与 operations 至少提供一个） |
| `operations` | object | 否* | 相对样式操作（与 styles 至少提供一个） |

*注意：`styles` 和 `operations` 至少必须提供一个。

#### 目标选择器 (target)

| 值 | 描述 |
|------|------|
| `'selected'` | 当前选中的所有图形元素 |
| `'edges'` | 画布上所有的边（连接线） |
| `'vertices'` | 画布上所有的顶点（形状） |
| `'all'` | 画布上所有的图形元素 |

#### 样式属性 (styles)

##### 颜色属性

| 属性名 | 类型 | 描述 | 示例 |
|--------|------|------|------|
| `fillColor` | string | 填充颜色 | `'#FF0000'`, `'red'`, `'none'` |
| `strokeColor` | string | 边框颜色 | `'#0000FF'`, `'blue'` |
| `fontColor` | string | 字体颜色 | `'#000000'`, `'black'` |
| `gradientColor` | string | 渐变颜色 | `'#00FF00'` |
| `labelBackgroundColor` | string | 标签背景颜色 | `'#FFFFFF'` |
| `labelBorderColor` | string | 标签边框颜色 | `'#CCCCCC'` |
| `shadowColor` | string | 阴影颜色 | `'#888888'` |
| `swimlaneFillColor` | string | 泳道填充颜色 | `'#E0E0E0'` |

**注意**：颜色属性只支持绝对值设置，不支持相对操作。

##### 数值属性

| 属性名 | 类型 | 范围 | 描述 |
|--------|------|------|------|
| `strokeWidth` | number | ≥ 0 | 线条粗细（像素） |
| `fontSize` | number | ≥ 1 | 字体大小（点） |
| `opacity` | number | 0-100 | 整体透明度（百分比） |
| `fillOpacity` | number | 0-100 | 填充透明度 |
| `strokeOpacity` | number | 0-100 | 边框透明度 |
| `textOpacity` | number | 0-100 | 文字透明度 |
| `arcSize` | number | 0-100 | 圆角大小 |
| `rotation` | number | 0-360 | 旋转角度（度） |
| `spacing` | number | - | 内边距 |
| `spacingTop` | number | - | 上内边距 |
| `spacingBottom` | number | - | 下内边距 |
| `spacingLeft` | number | - | 左内边距 |
| `spacingRight` | number | - | 右内边距 |

##### 开关属性

| 属性名 | 类型 | 描述 |
|--------|------|------|
| `dashed` | 0 \| 1 | 虚线样式 |
| `rounded` | 0 \| 1 | 圆角样式 |
| `curved` | 0 \| 1 | 曲线样式（仅边） |
| `shadow` | 0 \| 1 | 阴影效果 |
| `editable` | 0 \| 1 | 是否可编辑 |
| `movable` | 0 \| 1 | 是否可移动 |
| `resizable` | 0 \| 1 | 是否可调整大小 |
| `rotatable` | 0 \| 1 | 是否可旋转 |
| `deletable` | 0 \| 1 | 是否可删除 |

##### 枚举属性

| 属性名 | 可选值 | 描述 |
|--------|--------|------|
| `align` | `'left'`, `'center'`, `'right'` | 水平对齐 |
| `verticalAlign` | `'top'`, `'middle'`, `'bottom'` | 垂直对齐 |
| `labelPosition` | `'left'`, `'center'`, `'right'` | 标签位置 |
| `verticalLabelPosition` | `'top'`, `'middle'`, `'bottom'` | 标签垂直位置 |

##### 箭头属性（仅边元素）

| 属性名 | 类型 | 描述 |
|--------|------|------|
| `startArrow` | string | 起点箭头类型 |
| `endArrow` | string | 终点箭头类型 |
| `startSize` | number | 起点箭头大小 |
| `endSize` | number | 终点箭头大小 |
| `startFill` | 0 \| 1 | 起点箭头填充 |
| `endFill` | 0 \| 1 | 终点箭头填充 |

**箭头类型可选值**：
- `'none'` - 无箭头
- `'classic'` - 经典箭头
- `'classicThin'` - 细经典箭头
- `'block'` - 块状箭头
- `'blockThin'` - 细块状箭头
- `'open'` - 开放箭头
- `'openThin'` - 细开放箭头
- `'oval'` - 椭圆箭头
- `'diamond'` - 菱形箭头
- `'diamondThin'` - 细菱形箭头

**注意**：箭头属性应用于非边元素时会被自动忽略。

##### 字体属性

| 属性名 | 类型 | 描述 |
|--------|------|------|
| `fontFamily` | string | 字体名称 |
| `fontStyle` | number | 字体样式（位掩码：1=粗体, 2=斜体, 4=下划线） |

#### 相对操作 (operations)

相对操作允许在不知道当前值的情况下修改数值属性。

##### 操作类型

| 操作 | 描述 | 示例 |
|------|------|------|
| `'set'` | 设置为指定值 | `{ op: 'set', value: 10 }` |
| `'increase'` | 增加指定值 | `{ op: 'increase', value: 5 }` |
| `'decrease'` | 减少指定值 | `{ op: 'decrease', value: 3 }` |
| `'multiply'` | 乘以指定倍数 | `{ op: 'multiply', value: 1.5 }` |

##### 操作格式

```typescript
{
    [propertyName: string]: {
        op: 'set' | 'increase' | 'decrease' | 'multiply';
        value: number;
    }
}
```

##### 操作限制

- **颜色属性**：只支持 `'set'` 操作，不支持 `increase`、`decrease`、`multiply`
- **数值属性**：支持所有操作类型
- **值范围**：操作结果会自动限制在有效范围内

#### 请求示例

**设置绝对样式值**：
```javascript
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        fillColor: '#FF0000',
        strokeWidth: 3,
        rounded: 1,
        shadow: 1
    }
}
```

**使用相对操作**：
```javascript
{
    action: 'modifyStyle',
    target: 'edges',
    operations: {
        strokeWidth: { op: 'increase', value: 2 },
        opacity: { op: 'decrease', value: 20 }
    }
}
```

**混合使用绝对值和相对操作**：
```javascript
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        strokeColor: '#0000FF',
        dashed: 1
    },
    operations: {
        strokeWidth: { op: 'multiply', value: 2 },
        fontSize: { op: 'increase', value: 4 }
    }
}
```

**修改所有边的箭头**：
```javascript
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

**设置文字对齐**：
```javascript
{
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        align: 'center',
        verticalAlign: 'middle',
        fontColor: '#333333'
    }
}
```

### 响应消息

#### 基本格式

```typescript
interface ModifyStyleResponse {
    event: 'modifyStyle';            // 事件类型
    status: 'ok' | 'error';          // 状态
    error?: string;                  // 错误描述（仅在 status 为 'error' 时）
    errorCode?: string;              // 错误代码（仅在 status 为 'error' 时）
    data?: {                         // 成功时的额外数据
        modifiedCount: number;       // 修改的元素数量
        failedCount?: number;        // 失败的元素数量（部分失败时）
        errors?: Array<{             // 错误详情（部分失败时）
            property: string;
            error: string;
        }>;
    };
}
```

#### 字段说明

| 字段 | 类型 | 描述 |
|------|------|------|
| `event` | string | 固定为 `'modifyStyle'` |
| `status` | string | `'ok'` 表示成功，`'error'` 表示失败 |
| `error` | string | 错误描述信息（仅在失败时） |
| `errorCode` | string | 错误代码（仅在失败时，见错误代码表） |
| `data` | object | 成功时的额外数据 |
| `data.modifiedCount` | number | 成功修改的元素数量 |
| `data.failedCount` | number | 失败的元素数量（可选） |
| `data.errors` | array | 错误详情列表（可选） |

#### 响应示例

**成功响应**：
```javascript
{
    event: 'modifyStyle',
    status: 'ok',
    data: {
        modifiedCount: 5
    }
}
```

**部分成功响应**：
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

**错误响应**：
```javascript
{
    event: 'modifyStyle',
    status: 'error',
    error: 'Invalid target: unknown',
    errorCode: 'INVALID_TARGET'
}
```

## 错误代码

### 错误代码表

| 错误代码 | HTTP 类比 | 描述 | 可能原因 |
|---------|----------|------|---------|
| `INVALID_FORMAT` | 400 | 消息格式无效 | 缺少必需字段或字段类型错误 |
| `INVALID_TARGET` | 400 | 无效的目标选择器 | target 不是有效值 |
| `NO_TARGET_CELLS` | 404 | 没有目标元素 | 目标选择器返回空集合 |
| `INVALID_PROPERTY` | 400 | 无效的属性名 | 属性名不被支持 |
| `INVALID_VALUE` | 400 | 无效的属性值 | 属性值类型或范围错误 |
| `INVALID_OPERATION` | 400 | 无效的操作类型 | op 不是有效值 |
| `UNSUPPORTED_OPERATION` | 422 | 不支持的操作 | 对颜色属性使用相对操作 |
| `ORIGIN_DENIED` | 403 | 来源被拒绝 | 消息来源不在允许列表中 |

### 错误处理示例

```javascript
window.addEventListener('message', function(evt) {
    const data = JSON.parse(evt.data);
    
    if (data.event === 'modifyStyle') {
        if (data.status === 'error') {
            switch (data.errorCode) {
                case 'INVALID_TARGET':
                    console.error('无效的目标选择器：', data.error);
                    break;
                case 'NO_TARGET_CELLS':
                    console.warn('没有找到目标元素');
                    break;
                case 'UNSUPPORTED_OPERATION':
                    console.error('不支持的操作：', data.error);
                    break;
                case 'ORIGIN_DENIED':
                    console.error('安全错误：来源被拒绝');
                    break;
                default:
                    console.error('未知错误：', data.error);
            }
        } else {
            console.log('操作成功！');
            if (data.data.errors && data.data.errors.length > 0) {
                console.warn('部分属性修改失败：', data.data.errors);
            }
        }
    }
});
```

## 使用场景

### 场景 1：修改选中元素颜色

```javascript
// 用户选中一些图形后，点击颜色按钮
function changeColor(color) {
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'selected',
        styles: {
            fillColor: color
        }
    }), '*');
}
```

### 场景 2：加粗所有连接线

```javascript
// 一键加粗所有边
function thickenAllEdges() {
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'edges',
        operations: {
            strokeWidth: { op: 'increase', value: 2 }
        }
    }), '*');
}
```

### 场景 3：应用主题样式

```javascript
// 应用深色主题
function applyDarkTheme() {
    // 修改所有顶点
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'vertices',
        styles: {
            fillColor: '#2C2C2C',
            strokeColor: '#FFFFFF',
            fontColor: '#FFFFFF'
        }
    }), '*');
    
    // 修改所有边
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'edges',
        styles: {
            strokeColor: '#FFFFFF'
        }
    }), '*');
}
```

### 场景 4：AI 助手集成

```javascript
// AI 根据用户自然语言生成样式指令
async function handleUserCommand(userText) {
    // 例如用户说："把选中的改成红色并加粗"
    
    // 调用 AI API 将自然语言转换为样式指令
    const response = await fetch('/api/ai/style-command', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: userText })
    });
    
    const command = await response.json();
    // AI 返回：
    // {
    //     action: 'modifyStyle',
    //     target: 'selected',
    //     styles: { fillColor: '#FF0000' },
    //     operations: { strokeWidth: { op: 'increase', value: 2 } }
    // }
    
    // 执行样式修改
    iframe.contentWindow.postMessage(JSON.stringify(command), '*');
}
```

### 场景 5：批量样式调整

```javascript
// 一次性应用多个样式
function applyComplexStyle() {
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'selected',
        styles: {
            fillColor: '#0000FF',
            strokeColor: '#FFFFFF',
            rounded: 1,
            shadow: 1,
            fontColor: '#FFFFFF',
            align: 'center',
            verticalAlign: 'middle'
        },
        operations: {
            strokeWidth: { op: 'set', value: 3 },
            fontSize: { op: 'multiply', value: 1.2 },
            opacity: { op: 'set', value: 90 }
        }
    }), '*');
}
```

## 配置选项

### iframe URL 参数

可以通过 URL 参数配置 draw.io iframe 的行为：

```html
<iframe 
    src="https://app.diagrams.net/?embed=1&proto=json&allowedOrigins=https://example.com"
    width="100%" 
    height="600">
</iframe>
```

#### 可用参数

| 参数 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `embed` | number | 0 | 设置为 1 启用嵌入模式 |
| `proto` | string | - | 设置为 `json` 启用 JSON 协议 |
| `allowedOrigins` | string | `*` | 允许的消息来源，逗号分隔 |
| `debugMode` | boolean | false | 启用调试模式，输出详细日志 |

## 安全性

### Origin 验证

为了安全起见，建议在生产环境中配置允许的 origin 列表：

```javascript
// URL 参数方式
const iframeSrc = 'https://app.diagrams.net/?embed=1&proto=json&allowedOrigins=https://example.com';
```

### 输入验证

系统会自动验证：
- 所有字段的类型和范围
- 属性名的有效性
- 操作类型的有效性
- 枚举值的有效性

### 值范围限制

数值属性会自动限制在有效范围内：
- 透明度：0-100
- 旋转角度：0-360
- 线条粗细：≥ 0
- 字体大小：≥ 1

## 撤销支持

所有样式修改操作都支持撤销：

```javascript
// 用户可以通过 Ctrl+Z 或菜单撤销样式修改
// 批量修改会作为一个操作记录，一次撤销恢复所有属性
```

## 性能考虑

### 批量操作

系统使用 `model.beginUpdate()` 和 `model.endUpdate()` 包装所有修改，确保：
- 只触发一次视图更新
- 支持原子撤销
- 提高性能

### 最佳实践

1. **批量修改**：尽可能在一次消息中修改多个属性
2. **目标选择**：使用精确的目标选择器减少不必要的处理
3. **错误处理**：始终监听响应消息并处理错误

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

- **API 版本**：1.0.0
- **最后更新**：2024-12
- **兼容性**：draw.io 版本 22.0.0+

## 相关资源

- [draw.io 官方网站](https://www.diagrams.net/)
- [样式参考文档](./DRAWIO_STYLE_REFERENCE.md)
- [集成指南](./STYLE_MODIFICATION_INTEGRATION_GUIDE.md)
- [Mermaid 集成 API](./MERMAID_IFRAME_API.md)
