# Draw.io 样式属性参考文档

本文档列出了 Draw.io (mxGraph) 支持的所有样式属性，可用于 AI 生成图表修改指令。

## 颜色属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `fillColor` | 填充颜色 | HTML颜色名或HEX | `#FF0000`, `red`, `none` |
| `strokeColor` | 边框/线条颜色 | HTML颜色名或HEX | `#0000FF`, `blue`, `none` |
| `fontColor` | 文字颜色 | HTML颜色名或HEX | `#000000`, `black` |
| `gradientColor` | 渐变色 | HTML颜色名或HEX | `#FFFFFF` |
| `labelBackgroundColor` | 标签背景色 | HTML颜色名或HEX | `#FFFFCC` |
| `labelBorderColor` | 标签边框色 | HTML颜色名或HEX | `#000000` |
| `shadowColor` | 阴影颜色 | HTML颜色名或HEX | `#808080` |
| `swimlaneFillColor` | 泳道背景色 | HTML颜色名或HEX | `#E0E0E0` |

## 线条属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `strokeWidth` | 线条粗细 | 数字(>=1) | `1`, `2`, `3`, `5` |
| `dashed` | 是否虚线 | 0或1 | `0`=实线, `1`=虚线 |
| `dashPattern` | 虚线样式 | 空格分隔的数字 | `"3 3"`, `"5 2 2 2"` |
| `rounded` | 圆角 | 0或1 | `0`=直角, `1`=圆角 |
| `curved` | 曲线(仅边) | 0或1 | `0`=直线, `1`=曲线 |
| `arcSize` | 圆角大小 | 数字(0-100) | `10`, `20`, `50` |

## 透明度属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `opacity` | 整体透明度 | 0-100 | `100`=不透明, `50`=半透明 |
| `fillOpacity` | 填充透明度 | 0-100 | `80` |
| `strokeOpacity` | 边框透明度 | 0-100 | `100` |
| `textOpacity` | 文字透明度 | 0-100 | `100` |

## 字体属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `fontSize` | 字体大小 | 数字(px) | `12`, `14`, `16`, `20` |
| `fontFamily` | 字体 | 字体名称 | `"Arial"`, `"Times New Roman"` |
| `fontStyle` | 字体样式 | 数字(位运算) | `0`=正常, `1`=粗体, `2`=斜体, `3`=粗斜体, `4`=下划线 |
| `fontColor` | 字体颜色 | HTML颜色名或HEX | `#000000` |

### fontStyle 值说明
- `0` = 正常
- `1` = 粗体 (Bold)
- `2` = 斜体 (Italic)
- `3` = 粗斜体 (Bold + Italic)
- `4` = 下划线 (Underline)
- `5` = 粗体+下划线
- `6` = 斜体+下划线
- `7` = 粗斜体+下划线

## 对齐属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `align` | 水平对齐 | left/center/right | `"center"` |
| `verticalAlign` | 垂直对齐 | top/middle/bottom | `"middle"` |
| `labelPosition` | 标签水平位置 | left/center/right | `"center"` |
| `verticalLabelPosition` | 标签垂直位置 | top/middle/bottom | `"middle"` |

## 箭头属性 (仅用于边/连接线)

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `startArrow` | 起点箭头 | 箭头类型 | `"none"`, `"classic"`, `"block"` |
| `endArrow` | 终点箭头 | 箭头类型 | `"none"`, `"classic"`, `"block"` |
| `startSize` | 起点箭头大小 | 数字 | `6`, `8`, `10` |
| `endSize` | 终点箭头大小 | 数字 | `6`, `8`, `10` |
| `startFill` | 起点箭头填充 | 0或1 | `1`=填充, `0`=空心 |
| `endFill` | 终点箭头填充 | 0或1 | `1`=填充, `0`=空心 |

### 箭头类型
- `none` - 无箭头
- `classic` - 经典箭头
- `classicThin` - 细经典箭头
- `block` - 方块箭头
- `blockThin` - 细方块箭头
- `open` - 开放箭头
- `openThin` - 细开放箭头
- `oval` - 椭圆
- `diamond` - 菱形
- `diamondThin` - 细菱形

## 阴影属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `shadow` | 是否显示阴影 | 0或1 | `1`=显示 |
| `shadowColor` | 阴影颜色 | HTML颜色名或HEX | `#808080` |
| `shadowOpacity` | 阴影透明度 | 0-100 | `50` |
| `shadowOffsetX` | 阴影X偏移 | 数字 | `3` |
| `shadowOffsetY` | 阴影Y偏移 | 数字 | `3` |
| `shadowBlur` | 阴影模糊 | 数字 | `5` |

## 尺寸和位置属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `rotation` | 旋转角度 | 0-360 | `0`, `90`, `180`, `270` |
| `spacing` | 内边距 | 数字 | `5`, `10` |
| `spacingTop` | 上内边距 | 数字 | `5` |
| `spacingBottom` | 下内边距 | 数字 | `5` |
| `spacingLeft` | 左内边距 | 数字 | `5` |
| `spacingRight` | 右内边距 | 数字 | `5` |

## 行为属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `editable` | 是否可编辑 | 0或1 | `1`=可编辑 |
| `movable` | 是否可移动 | 0或1 | `1`=可移动 |
| `resizable` | 是否可调整大小 | 0或1 | `1`=可调整 |
| `rotatable` | 是否可旋转 | 0或1 | `1`=可旋转 |
| `deletable` | 是否可删除 | 0或1 | `1`=可删除 |
| `bendable` | 边是否可弯曲 | 0或1 | `1`=可弯曲 |
| `foldable` | 是否可折叠 | 0或1 | `1`=可折叠 |

## 形状属性

| 属性名 | 说明 | 值类型 | 示例 |
|-------|------|-------|------|
| `shape` | 形状类型 | 形状名称 | 见下方形状列表 |
| `perimeter` | 边界类型 | 边界名称 | `"rectanglePerimeter"` |

### 常用形状类型
- `rectangle` - 矩形
- `ellipse` - 椭圆
- `rhombus` - 菱形
- `triangle` - 三角形
- `hexagon` - 六边形
- `cylinder` - 圆柱
- `actor` - 人形
- `cloud` - 云朵
- `parallelogram` - 平行四边形
- `document` - 文档
- `callout` - 标注

---

## AI 指令生成示例

### 示例1：修改颜色
用户输入："把选中的改成红色"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "styles": {
    "fillColor": "#FF0000"
  }
}
```

### 示例2：加粗线条
用户输入："把所有线条加粗"
```json
{
  "action": "modifyStyle",
  "target": "edges",
  "styles": {
    "strokeWidth": 3
  }
}
```

### 示例3：改成虚线
用户输入："把线条改成虚线"
```json
{
  "action": "modifyStyle",
  "target": "edges",
  "styles": {
    "dashed": 1
  }
}
```

### 示例4：修改字体
用户输入："把文字放大加粗"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "styles": {
    "fontSize": 16,
    "fontStyle": 1
  }
}
```

### 示例5：添加阴影
用户输入："给图形加上阴影"
```json
{
  "action": "modifyStyle",
  "target": "vertices",
  "styles": {
    "shadow": 1
  }
}
```

### 示例6：组合修改
用户输入："把选中的改成蓝色圆角矩形，边框加粗"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "styles": {
    "fillColor": "#0000FF",
    "rounded": 1,
    "strokeWidth": 3
  }
}
```

---

## 颜色参考

### 常用颜色名称映射
| 中文 | 英文 | HEX |
|-----|------|-----|
| 红色 | red | #FF0000 |
| 蓝色 | blue | #0000FF |
| 绿色 | green | #00FF00 |
| 黄色 | yellow | #FFFF00 |
| 橙色 | orange | #FFA500 |
| 紫色 | purple | #800080 |
| 粉色 | pink | #FFC0CB |
| 黑色 | black | #000000 |
| 白色 | white | #FFFFFF |
| 灰色 | gray | #808080 |
| 棕色 | brown | #A52A2A |
| 青色 | cyan | #00FFFF |

---

## target 目标选择

| 值 | 说明 |
|----|------|
| `selected` | 当前选中的元素 |
| `edges` | 所有边（连接线） |
| `vertices` | 所有顶点（形状） |
| `all` | 所有元素 |

---

## 相对操作 (operations)

对于数值类属性（如线条粗细、字体大小），支持相对操作，避免 AI 不知道当前值而设置不合理的绝对值。

### 操作类型

| 操作 | 说明 | 适用场景 |
|-----|------|---------|
| `set` | 设置绝对值 | 明确指定具体数值时 |
| `increase` | 在当前值基础上增加 | "加粗"、"放大"、"加深" |
| `decrease` | 在当前值基础上减少 | "变细"、"缩小"、"变淡" |
| `multiply` | 当前值乘以倍数 | "翻倍"、"放大两倍" |

### 操作格式

```json
{
  "action": "modifyStyle",
  "target": "edges",
  "operations": {
    "属性名": { "op": "操作类型", "value": 数值 }
  }
}
```

### 相对操作示例

#### 示例1：加粗线条
用户输入："把线条加粗一点"
```json
{
  "action": "modifyStyle",
  "target": "edges",
  "operations": {
    "strokeWidth": { "op": "increase", "value": 2 }
  }
}
```

#### 示例2：线条变细
用户输入："把线条变细"
```json
{
  "action": "modifyStyle",
  "target": "edges",
  "operations": {
    "strokeWidth": { "op": "decrease", "value": 1 }
  }
}
```

#### 示例3：字体放大
用户输入："把文字放大"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "operations": {
    "fontSize": { "op": "increase", "value": 4 }
  }
}
```

#### 示例4：字体翻倍
用户输入："把文字放大两倍"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "operations": {
    "fontSize": { "op": "multiply", "value": 2 }
  }
}
```

#### 示例5：降低透明度
用户输入："让图形更透明一些"
```json
{
  "action": "modifyStyle",
  "target": "vertices",
  "operations": {
    "opacity": { "op": "decrease", "value": 30 }
  }
}
```

#### 示例6：设置绝对值
用户输入："把线条设为5像素"
```json
{
  "action": "modifyStyle",
  "target": "edges",
  "operations": {
    "strokeWidth": { "op": "set", "value": 5 }
  }
}
```

### 混合使用 styles 和 operations

可以同时使用绝对值和相对操作：

用户输入："把选中的线条改成红色并加粗"
```json
{
  "action": "modifyStyle",
  "target": "selected",
  "styles": {
    "strokeColor": "#FF0000"
  },
  "operations": {
    "strokeWidth": { "op": "increase", "value": 2 }
  }
}
```

### 适合相对操作的属性

| 属性 | 建议操作 | 说明 |
|-----|---------|------|
| `strokeWidth` | increase/decrease | 线条粗细 |
| `fontSize` | increase/decrease/multiply | 字体大小 |
| `opacity` | increase/decrease | 透明度 |
| `fillOpacity` | increase/decrease | 填充透明度 |
| `strokeOpacity` | increase/decrease | 边框透明度 |
| `arcSize` | increase/decrease | 圆角大小 |
| `startSize` | increase/decrease | 起点箭头大小 |
| `endSize` | increase/decrease | 终点箭头大小 |
| `spacing` | increase/decrease | 内边距 |
| `rotation` | increase/decrease | 旋转角度 |

### 适合绝对值的属性

| 属性 | 说明 |
|-----|------|
| 所有颜色属性 | fillColor, strokeColor, fontColor 等 |
| 开关类属性 | dashed, rounded, shadow 等 (0/1) |
| 对齐属性 | align, verticalAlign 等 |
| 箭头类型 | startArrow, endArrow |
| 形状类型 | shape |

---

## AI 指令选择指南

| 用户表达 | 推荐方式 | 示例 |
|---------|---------|------|
| "改成红色" | styles (绝对值) | `"fillColor": "#FF0000"` |
| "加粗" | operations (相对) | `"strokeWidth": {"op": "increase", "value": 2}` |
| "设为5像素" | operations (set) | `"strokeWidth": {"op": "set", "value": 5}` |
| "放大两倍" | operations (multiply) | `"fontSize": {"op": "multiply", "value": 2}` |
| "变成虚线" | styles (绝对值) | `"dashed": 1` |
| "更透明" | operations (相对) | `"opacity": {"op": "decrease", "value": 20}` |
