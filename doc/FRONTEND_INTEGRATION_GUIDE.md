# 前端系统集成指南：Draw.io 样式修改功能

## 概述

本指南面向前端开发者，详细说明如何在您的前端系统中集成 draw.io 的样式修改功能。通过这个功能，您的应用可以通过简单的 JSON 消息自动修改嵌入的 draw.io 图形样式，实现智能化的图形编辑体验。

## 适用场景

- **AI 助手集成**：让 AI 根据用户自然语言指令修改图形样式
- **主题切换**：一键应用深色/浅色主题
- **批量样式调整**：统一修改多个元素的样式
- **自动化工作流**：根据业务逻辑自动调整图形外观
- **可视化定制**：为不同用户群体提供定制化的图形样式

## 快速开始

### 步骤 1：嵌入 Draw.io iframe

在您的 HTML 页面中添加 draw.io iframe：

```html
<!DOCTYPE html>
<html>
<head>
    <title>我的应用</title>
</head>
<body>
    <iframe 
        id="drawio-iframe"
        src="https://app.diagrams.net/?embed=1&proto=json"
        width="100%" 
        height="600"
        frameborder="0">
    </iframe>
</body>
</html>
```

**重要参数**：
- `embed=1`：启用嵌入模式
- `proto=json`：启用 JSON 协议

### 步骤 2：添加消息监听器

```javascript
window.addEventListener('message', function(evt) {
    const iframe = document.getElementById('drawio-iframe');
    
    // 验证消息来源
    if (evt.source !== iframe.contentWindow) {
        return;
    }
    
    // 解析消息数据
    let data;
    try {
        data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
    } catch (e) {
        console.error('无法解析消息：', e);
        return;
    }
    
    // 处理样式修改响应
    if (data.event === 'modifyStyle') {
        if (data.status === 'ok') {
            console.log('✓ 操作成功！修改了', data.data.modifiedCount, '个元素');
            // 显示成功提示
            showSuccessMessage(`成功修改了 ${data.data.modifiedCount} 个元素`);
        } else {
            console.error('✗ 样式修改失败：', data.error);
            // 显示错误提示
            showErrorMessage(`修改失败：${data.error}`);
        }
    }
});
```

### 步骤 3：发送样式修改请求

```javascript
function modifyStyle(target, styles, operations) {
    const iframe = document.getElementById('drawio-iframe');
    
    const message = {
        action: 'modifyStyle',
        target: target
    };
    
    if (styles) {
        message.styles = styles;
    }
    
    if (operations) {
        message.operations = operations;
    }
    
    iframe.contentWindow.postMessage(JSON.stringify(message), '*');
}

// 使用示例：修改选中元素的颜色
modifyStyle('selected', { fillColor: '#FF0000' });
```

## 消息格式详解

### 请求消息结构

```javascript
{
    action: 'modifyStyle',           // 必需：固定值
    target: 'selected',              // 必需：目标选择器
    styles: {                        // 可选：绝对样式值
        fillColor: '#FF0000',
        strokeWidth: 3
    },
    operations: {                    // 可选：相对操作
        fontSize: { 
            op: 'increase', 
            value: 4 
        }
    }
}
```

**字段说明**：

| 字段 | 类型 | 必需 | 描述 |
|------|------|------|------|
| `action` | string | 是 | 必须为 `'modifyStyle'` |
| `target` | string | 是 | 目标选择器：`selected`/`edges`/`vertices`/`all` |
| `styles` | object | 否* | 绝对样式值设置 |
| `operations` | object | 否* | 相对样式操作 |

*注意：`styles` 和 `operations` 至少必须提供一个。

### 响应消息结构

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

**错误响应**：
```javascript
{
    event: 'modifyStyle',
    status: 'error',
    error: 'Invalid target: unknown',
    errorCode: 'INVALID_TARGET'
}
```

## 完整集成示例

### Vanilla JavaScript 示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>Draw.io 样式修改示例</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 20px;
        }
        .controls {
            margin-bottom: 20px;
            padding: 15px;
            background: #f5f5f5;
            border-radius: 4px;
        }
        button {
            padding: 8px 16px;
            margin: 5px;
            background-color: #007bff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        button:hover {
            background-color: #0056b3;
        }
        .status {
            margin-top: 10px;
            padding: 10px;
            border-radius: 4px;
        }
        .status.success {
            background-color: #d4edda;
            color: #155724;
        }
        .status.error {
            background-color: #f8d7da;
            color: #721c24;
        }
    </style>
</head>
<body>
    <h1>Draw.io 样式修改示例</h1>
    
    <div class="controls">
        <h2>样式控制</h2>
        <button onclick="changeColor('#FF0000')">红色</button>
        <button onclick="changeColor('#0000FF')">蓝色</button>
        <button onclick="changeColor('#00FF00')">绿色</button>
        <button onclick="thickenLines()">加粗线条</button>
        <button onclick="addShadow()">添加阴影</button>
        <button onclick="makeRounded()">圆角化</button>
        <button onclick="applyDarkTheme()">深色主题</button>
        <div id="status"></div>
    </div>
    
    <iframe 
        id="drawio-iframe"
        src="https://app.diagrams.net/?embed=1&proto=json"
        width="100%" 
        height="600"
        frameborder="0">
    </iframe>

    <script>
        const iframe = document.getElementById('drawio-iframe');
        const statusDiv = document.getElementById('status');
        
        // 监听响应
        window.addEventListener('message', function(evt) {
            if (evt.source !== iframe.contentWindow) return;
            
            let data;
            try {
                data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
            } catch (e) {
                return;
            }
            
            if (data.event === 'modifyStyle') {
                if (data.status === 'ok') {
                    showStatus('success', `✓ 成功修改了 ${data.data.modifiedCount} 个元素`);
                } else {
                    showStatus('error', `✗ 错误：${data.error}`);
                }
            }
        });
        
        // 发送样式修改消息
        function sendMessage(message) {
            iframe.contentWindow.postMessage(JSON.stringify(message), '*');
        }
        
        // 修改颜色
        function changeColor(color) {
            sendMessage({
                action: 'modifyStyle',
                target: 'selected',
                styles: { fillColor: color }
            });
        }
        
        // 加粗线条
        function thickenLines() {
            sendMessage({
                action: 'modifyStyle',
                target: 'selected',
                operations: {
                    strokeWidth: { op: 'increase', value: 2 }
                }
            });
        }
        
        // 添加阴影
        function addShadow() {
            sendMessage({
                action: 'modifyStyle',
                target: 'selected',
                styles: { shadow: 1 }
            });
        }
        
        // 圆角化
        function makeRounded() {
            sendMessage({
                action: 'modifyStyle',
                target: 'selected',
                styles: { rounded: 1 }
            });
        }
        
        // 应用深色主题
        function applyDarkTheme() {
            sendMessage({
                action: 'modifyStyle',
                target: 'all',
                styles: {
                    fillColor: '#2C2C2C',
                    strokeColor: '#FFFFFF',
                    fontColor: '#FFFFFF'
                }
            });
        }
        
        // 显示状态
        function showStatus(type, message) {
            statusDiv.className = 'status ' + type;
            statusDiv.textContent = message;
            statusDiv.style.display = 'block';
        }
    </script>
</body>
</html>
```

### React 集成示例

```jsx
import React, { useRef, useEffect, useState } from 'react';

function DrawioStyleEditor() {
    const iframeRef = useRef(null);
    const [status, setStatus] = useState({ type: '', message: '' });
    const [target, setTarget] = useState('selected');

    useEffect(() => {
        const handleMessage = (evt) => {
            if (evt.source !== iframeRef.current?.contentWindow) return;

            let data;
            try {
                data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
            } catch (e) {
                return;
            }

            if (data.event === 'modifyStyle') {
                if (data.status === 'ok') {
                    setStatus({
                        type: 'success',
                        message: `✓ 成功修改了 ${data.data.modifiedCount} 个元素`
                    });
                } else {
                    setStatus({
                        type: 'error',
                        message: `✗ 错误：${data.error}`
                    });
                }
            }
        };

        window.addEventListener('message', handleMessage);
        return () => window.removeEventListener('message', handleMessage);
    }, []);

    const sendMessage = (message) => {
        iframeRef.current?.contentWindow.postMessage(JSON.stringify(message), '*');
    };

    const changeColor = (color) => {
        sendMessage({
            action: 'modifyStyle',
            target,
            styles: { fillColor: color }
        });
    };

    const thickenLines = () => {
        sendMessage({
            action: 'modifyStyle',
            target,
            operations: {
                strokeWidth: { op: 'increase', value: 2 }
            }
        });
    };

    const applyDarkTheme = () => {
        sendMessage({
            action: 'modifyStyle',
            target: 'all',
            styles: {
                fillColor: '#2C2C2C',
                strokeColor: '#FFFFFF',
                fontColor: '#FFFFFF'
            }
        });
    };

    return (
        <div style={{ padding: '20px' }}>
            <h1>Draw.io 样式编辑器</h1>
            
            <div style={{ marginBottom: '20px', padding: '15px', background: '#f5f5f5' }}>
                <h2>样式控制</h2>
                
                <div style={{ marginBottom: '10px' }}>
                    <label>目标：</label>
                    <select value={target} onChange={(e) => setTarget(e.target.value)}>
                        <option value="selected">选中的元素</option>
                        <option value="edges">所有边</option>
                        <option value="vertices">所有顶点</option>
                        <option value="all">所有元素</option>
                    </select>
                </div>
                
                <button onClick={() => changeColor('#FF0000')}>红色</button>
                <button onClick={() => changeColor('#0000FF')}>蓝色</button>
                <button onClick={() => changeColor('#00FF00')}>绿色</button>
                <button onClick={thickenLines}>加粗线条</button>
                <button onClick={applyDarkTheme}>深色主题</button>
                
                {status.message && (
                    <div style={{
                        marginTop: '10px',
                        padding: '10px',
                        borderRadius: '4px',
                        backgroundColor: status.type === 'success' ? '#d4edda' : '#f8d7da',
                        color: status.type === 'success' ? '#155724' : '#721c24'
                    }}>
                        {status.message}
                    </div>
                )}
            </div>
            
            <iframe
                ref={iframeRef}
                src="https://app.diagrams.net/?embed=1&proto=json"
                width="100%"
                height="600"
                frameBorder="0"
            />
        </div>
    );
}

export default DrawioStyleEditor;
```

### Vue.js 集成示例

```vue
<template>
  <div class="drawio-editor">
    <h1>Draw.io 样式编辑器</h1>
    
    <div class="controls">
      <h2>样式控制</h2>
      
      <div class="control-group">
        <label>目标：</label>
        <select v-model="target">
          <option value="selected">选中的元素</option>
          <option value="edges">所有边</option>
          <option value="vertices">所有顶点</option>
          <option value="all">所有元素</option>
        </select>
      </div>
      
      <button @click="changeColor('#FF0000')">红色</button>
      <button @click="changeColor('#0000FF')">蓝色</button>
      <button @click="changeColor('#00FF00')">绿色</button>
      <button @click="thickenLines">加粗线条</button>
      <button @click="applyDarkTheme">深色主题</button>
      
      <div v-if="status.message" :class="['status', status.type]">
        {{ status.message }}
      </div>
    </div>
    
    <iframe
      ref="iframe"
      src="https://app.diagrams.net/?embed=1&proto=json"
      width="100%"
      height="600"
      frameborder="0"
    />
  </div>
</template>

<script>
export default {
  name: 'DrawioEditor',
  data() {
    return {
      target: 'selected',
      status: { type: '', message: '' }
    };
  },
  mounted() {
    window.addEventListener('message', this.handleMessage);
  },
  beforeUnmount() {
    window.removeEventListener('message', this.handleMessage);
  },
  methods: {
    handleMessage(evt) {
      if (evt.source !== this.$refs.iframe?.contentWindow) return;
      
      let data;
      try {
        data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
      } catch (e) {
        return;
      }
      
      if (data.event === 'modifyStyle') {
        if (data.status === 'ok') {
          this.status = {
            type: 'success',
            message: `✓ 成功修改了 ${data.data.modifiedCount} 个元素`
          };
        } else {
          this.status = {
            type: 'error',
            message: `✗ 错误：${data.error}`
          };
        }
      }
    },
    sendMessage(message) {
      this.$refs.iframe?.contentWindow.postMessage(JSON.stringify(message), '*');
    },
    changeColor(color) {
      this.sendMessage({
        action: 'modifyStyle',
        target: this.target,
        styles: { fillColor: color }
      });
    },
    thickenLines() {
      this.sendMessage({
        action: 'modifyStyle',
        target: this.target,
        operations: {
          strokeWidth: { op: 'increase', value: 2 }
        }
      });
    },
    applyDarkTheme() {
      this.sendMessage({
        action: 'modifyStyle',
        target: 'all',
        styles: {
          fillColor: '#2C2C2C',
          strokeColor: '#FFFFFF',
          fontColor: '#FFFFFF'
        }
      });
    }
  }
};
</script>

<style scoped>
.drawio-editor {
  padding: 20px;
}
.controls {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 4px;
}
button {
  padding: 8px 16px;
  margin: 5px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
button:hover {
  background-color: #0056b3;
}
.status {
  margin-top: 10px;
  padding: 10px;
  border-radius: 4px;
}
.status.success {
  background-color: #d4edda;
  color: #155724;
}
.status.error {
  background-color: #f8d7da;
  color: #721c24;
}
</style>
```

## AI 助手集成

### 基本 AI 集成架构

```
用户输入 → AI 解析 → 生成样式指令 → 发送到 Draw.io → 应用样式 → 返回结果
```

### AI 集成完整示例

```javascript
class AIStyleAssistant {
    constructor(iframeId, aiApiUrl) {
        this.iframe = document.getElementById(iframeId);
        this.aiApiUrl = aiApiUrl;
        this.setupMessageListener();
    }
    
    setupMessageListener() {
        window.addEventListener('message', (evt) => {
            if (evt.source !== this.iframe.contentWindow) return;
            
            let data;
            try {
                data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
            } catch (e) {
                return;
            }
            
            if (data.event === 'modifyStyle') {
                this.handleStyleResponse(data);
            }
        });
    }
    
    handleStyleResponse(data) {
        if (data.status === 'ok') {
            this.onSuccess(data.data.modifiedCount);
        } else {
            this.onError(data.error, data.errorCode);
        }
    }
    
    async processUserCommand(userText) {
        try {
            // 显示加载状态
            this.onLoading();
            
            // 调用 AI API 将自然语言转换为样式指令
            const styleCommand = await this.callAI(userText);
            
            // 验证 AI 返回的指令
            if (!this.validateCommand(styleCommand)) {
                throw new Error('AI 返回的指令格式无效');
            }
            
            // 应用样式
            this.applyStyle(styleCommand);
            
        } catch (error) {
            this.onError(error.message);
        }
    }
    
    async callAI(userText) {
        const response = await fetch(this.aiApiUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer YOUR_API_KEY'
            },
            body: JSON.stringify({
                text: userText,
                context: 'drawio-style-modification'
            })
        });
        
        if (!response.ok) {
            throw new Error('AI API 调用失败');
        }
        
        const data = await response.json();
        return data.command;
    }
    
    validateCommand(command) {
        if (!command || typeof command !== 'object') return false;
        if (command.action !== 'modifyStyle') return false;
        
        const validTargets = ['selected', 'edges', 'vertices', 'all'];
        if (!validTargets.includes(command.target)) return false;
        
        if (!command.styles && !command.operations) return false;
        
        return true;
    }
    
    applyStyle(command) {
        this.iframe.contentWindow.postMessage(JSON.stringify(command), '*');
    }
    
    onLoading() {
        console.log('正在处理...');
    }
    
    onSuccess(count) {
        console.log(`成功修改了 ${count} 个元素`);
    }
    
    onError(error, code) {
        console.error(`错误：${error}`, code);
    }
}

// 使用示例
const assistant = new AIStyleAssistant('drawio-iframe', 'https://api.example.com/ai/style');

// 处理用户自然语言输入
assistant.processUserCommand('把选中的改成红色并加粗');
assistant.processUserCommand('给所有连接线添加箭头');
assistant.processUserCommand('应用深色主题');
```

### AI 提示词模板

```
系统提示词：
你是一个 draw.io 样式修改助手。用户会用自然语言描述他们想要的样式修改，你需要将其转换为 JSON 格式的样式修改指令。

指令格式：
{
    "action": "modifyStyle",
    "target": "selected" | "edges" | "vertices" | "all",
    "styles": { 属性名: 值 },
    "operations": { 属性名: { "op": "操作类型", "value": 数值 } }
}

可用的样式属性：
- 颜色：fillColor, strokeColor, fontColor（使用十六进制值如 #FF0000）
- 数值：strokeWidth, fontSize, opacity（0-100）
- 开关：dashed, rounded, shadow（0 或 1）
- 对齐：align (left/center/right), verticalAlign (top/middle/bottom)
- 箭头：startArrow, endArrow, startSize, endSize（仅用于边）

可用的操作类型：
- set: 设置为指定值
- increase: 增加指定值
- decrease: 减少指定值
- multiply: 乘以指定倍数

注意事项：
- 颜色属性只能使用 styles，不能使用 operations
- 箭头属性只对边元素有效
- target 为 'selected' 时需要用户已选择元素

示例：
用户："把选中的改成红色"
输出：{"action":"modifyStyle","target":"selected","styles":{"fillColor":"#FF0000"}}

用户："加粗所有连接线"
输出：{"action":"modifyStyle","target":"edges","operations":{"strokeWidth":{"op":"increase","value":2}}}

用户："给所有形状添加阴影并设置圆角"
输出：{"action":"modifyStyle","target":"vertices","styles":{"shadow":1,"rounded":1}}

用户："把字体放大一倍"
输出：{"action":"modifyStyle","target":"selected","operations":{"fontSize":{"op":"multiply","value":2}}}

用户："应用深色主题"
输出：{"action":"modifyStyle","target":"all","styles":{"fillColor":"#2C2C2C","strokeColor":"#FFFFFF","fontColor":"#FFFFFF"}}
```

## 高级用法

### 1. 封装样式修改类

```javascript
class DrawioStyleManager {
    constructor(iframeId) {
        this.iframe = document.getElementById(iframeId);
        this.listeners = [];
    }
    
    // 注册响应监听器
    onResponse(callback) {
        this.listeners.push(callback);
    }
    
    // 初始化消息监听
    init() {
        window.addEventListener('message', (evt) => {
            if (evt.source !== this.iframe.contentWindow) return;
            
            let data;
            try {
                data = typeof evt.data === 'string' ? JSON.parse(evt.data) : evt.data;
            } catch (e) {
                return;
            }
            
            if (data.event === 'modifyStyle') {
                this.listeners.forEach(callback => callback(data));
            }
        });
    }
    
    // 发送样式修改消息
    modify(target, styles, operations) {
        const message = {
            action: 'modifyStyle',
            target
        };
        
        if (styles) message.styles = styles;
        if (operations) message.operations = operations;
        
        this.iframe.contentWindow.postMessage(JSON.stringify(message), '*');
    }
    
    // 便捷方法：修改颜色
    setColor(target, fillColor, strokeColor) {
        const styles = {};
        if (fillColor) styles.fillColor = fillColor;
        if (strokeColor) styles.strokeColor = strokeColor;
        this.modify(target, styles);
    }
    
    // 便捷方法：调整线条粗细
    adjustStrokeWidth(target, operation, value) {
        this.modify(target, null, {
            strokeWidth: { op: operation, value }
        });
    }
    
    // 便捷方法：调整字体大小
    adjustFontSize(target, operation, value) {
        this.modify(target, null, {
            fontSize: { op: operation, value }
        });
    }
    
    // 便捷方法：添加效果
    addEffects(target, effects) {
        const styles = {};
        if (effects.shadow) styles.shadow = 1;
        if (effects.rounded) styles.rounded = 1;
        if (effects.dashed) styles.dashed = 1;
        this.modify(target, styles);
    }
    
    // 便捷方法：设置箭头
    setArrows(target, startArrow, endArrow) {
        const styles = {};
        if (startArrow) {
            styles.startArrow = startArrow;
            styles.startSize = 10;
            styles.startFill = 1;
        }
        if (endArrow) {
            styles.endArrow = endArrow;
            styles.endSize = 10;
            styles.endFill = 1;
        }
        this.modify(target, styles);
    }
    
    // 便捷方法：应用主题
    applyTheme(theme) {
        const themes = {
            dark: {
                target: 'all',
                styles: {
                    fillColor: '#2C2C2C',
                    strokeColor: '#FFFFFF',
                    fontColor: '#FFFFFF'
                }
            },
            light: {
                target: 'all',
                styles: {
                    fillColor: '#FFFFFF',
                    strokeColor: '#000000',
                    fontColor: '#000000'
                }
            },
            blue: {
                target: 'all',
                styles: {
                    fillColor: '#E3F2FD',
                    strokeColor: '#1976D2',
                    fontColor: '#0D47A1'
                }
            }
        };
        
        const themeConfig = themes[theme];
        if (themeConfig) {
            this.modify(themeConfig.target, themeConfig.styles);
        }
    }
}

// 使用示例
const styleManager = new DrawioStyleManager('drawio-iframe');
styleManager.init();

styleManager.onResponse((data) => {
    if (data.status === 'ok') {
        console.log('成功！');
    } else {
        console.error('失败：', data.error);
    }
});

// 使用便捷方法
styleManager.setColor('selected', '#FF0000', '#000000');
styleManager.adjustStrokeWidth('edges', 'increase', 2);
styleManager.addEffects('selected', { shadow: true, rounded: true });
styleManager.setArrows('edges', null, 'classic');
styleManager.applyTheme('dark');
```

### 2. 批量样式预设

```javascript
const stylePresets = {
    // 强调样式
    emphasis: {
        target: 'selected',
        styles: {
            fillColor: '#FFD700',
            strokeColor: '#FF0000',
            strokeWidth: 3,
            shadow: 1
        }
    },
    
    // 淡化样式
    fade: {
        target: 'selected',
        operations: {
            opacity: { op: 'set', value: 50 }
        }
    },
    
    // 专业样式
    professional: {
        target: 'selected',
        styles: {
            fillColor: '#FFFFFF',
            strokeColor: '#333333',
            strokeWidth: 2,
            rounded: 1,
            fontColor: '#333333'
        }
    },
    
    // 警告样式
    warning: {
        target: 'selected',
        styles: {
            fillColor: '#FFF3CD',
            strokeColor: '#FFC107',
            strokeWidth: 2,
            fontColor: '#856404'
        }
    },
    
    // 错误样式
    error: {
        target: 'selected',
        styles: {
            fillColor: '#F8D7DA',
            strokeColor: '#DC3545',
            strokeWidth: 2,
            fontColor: '#721C24'
        }
    },
    
    // 成功样式
    success: {
        target: 'selected',
        styles: {
            fillColor: '#D4EDDA',
            strokeColor: '#28A745',
            strokeWidth: 2,
            fontColor: '#155724'
        }
    }
};

// 应用预设
function applyPreset(presetName) {
    const preset = stylePresets[presetName];
    if (preset) {
        iframe.contentWindow.postMessage(JSON.stringify({
            action: 'modifyStyle',
            ...preset
        }), '*');
    }
}

// 使用示例
applyPreset('emphasis');
applyPreset('warning');
```

### 3. 动画效果模拟

```javascript
// 通过连续的样式修改模拟动画效果
async function animatePulse(target, duration = 1000) {
    const steps = 10;
    const interval = duration / steps;
    
    for (let i = 0; i < steps; i++) {
        const opacity = 100 - (i % 2) * 30;
        
        iframe.contentWindow.postMessage(JSON.stringify({
            action: 'modifyStyle',
            target,
            operations: {
                opacity: { op: 'set', value: opacity }
            }
        }), '*');
        
        await new Promise(resolve => setTimeout(resolve, interval));
    }
}

// 渐变颜色动画
async function animateColorTransition(target, fromColor, toColor, duration = 1000) {
    // 注意：这需要颜色插值计算
    // 这里简化为直接切换
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target,
        styles: { fillColor: toColor }
    }), '*');
}

// 使用示例
animatePulse('selected', 2000);
```

### 4. 条件样式应用

```javascript
// 根据条件应用不同样式
function applyConditionalStyle(target, condition) {
    let styles = {};
    
    if (condition.status === 'active') {
        styles.fillColor = '#28A745';
        styles.strokeColor = '#155724';
    } else if (condition.status === 'inactive') {
        styles.fillColor = '#6C757D';
        styles.strokeColor = '#495057';
    } else if (condition.status === 'error') {
        styles.fillColor = '#DC3545';
        styles.strokeColor = '#721C24';
    }
    
    if (condition.priority === 'high') {
        styles.strokeWidth = 4;
        styles.shadow = 1;
    } else if (condition.priority === 'low') {
        styles.strokeWidth = 1;
        styles.dashed = 1;
    }
    
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target,
        styles
    }), '*');
}

// 使用示例
applyConditionalStyle('selected', { status: 'active', priority: 'high' });
```

## 错误处理最佳实践

### 1. 完整的错误处理

```javascript
class StyleModificationHandler {
    constructor(iframeId) {
        this.iframe = document.getElementById(iframeId);
        this.pendingRequests = new Map();
        this.requestId = 0;
    }
    
    async modifyStyle(target, styles, operations) {
        return new Promise((resolve, reject) => {
            const id = this.requestId++;
            
            // 设置超时
            const timeout = setTimeout(() => {
                this.pendingRequests.delete(id);
                reject(new Error('请求超时'));
            }, 5000);
            
            // 保存请求
            this.pendingRequests.set(id, { resolve, reject, timeout });
            
            // 发送消息
            try {
                const message = {
                    action: 'modifyStyle',
                    target,
                    requestId: id
                };
                
                if (styles) message.styles = styles;
                if (operations) message.operations = operations;
                
                this.iframe.contentWindow.postMessage(JSON.stringify(message), '*');
            } catch (error) {
                clearTimeout(timeout);
                this.pendingRequests.delete(id);
                reject(error);
            }
        });
    }
    
    handleResponse(data) {
        const request = this.pendingRequests.get(data.requestId);
        if (!request) return;
        
        clearTimeout(request.timeout);
        this.pendingRequests.delete(data.requestId);
        
        if (data.status === 'ok') {
            request.resolve(data.data);
        } else {
            request.reject(new Error(data.error));
        }
    }
}

// 使用示例
const handler = new StyleModificationHandler('drawio-iframe');

try {
    const result = await handler.modifyStyle('selected', { fillColor: '#FF0000' });
    console.log('成功修改了', result.modifiedCount, '个元素');
} catch (error) {
    console.error('修改失败：', error.message);
}
```

### 2. 错误代码处理

```javascript
function handleStyleError(errorCode, error) {
    const errorMessages = {
        'INVALID_FORMAT': '消息格式无效，请检查参数',
        'INVALID_TARGET': '无效的目标选择器',
        'NO_TARGET_CELLS': '没有找到目标元素，请先选择元素',
        'INVALID_PROPERTY': '无效的属性名',
        'INVALID_VALUE': '无效的属性值',
        'INVALID_OPERATION': '无效的操作类型',
        'UNSUPPORTED_OPERATION': '不支持的操作（颜色属性不支持相对操作）',
        'ORIGIN_DENIED': '安全错误：消息来源被拒绝'
    };
    
    const message = errorMessages[errorCode] || error;
    
    // 显示用户友好的错误消息
    showErrorNotification(message);
    
    // 记录详细错误日志
    console.error(`样式修改错误 [${errorCode}]:`, error);
}
```

## 常见问题和解决方案

### 问题 1：没有目标元素

**症状**：收到 `NO_TARGET_CELLS` 错误

**原因**：
- 使用 `target: 'selected'` 但没有选中任何元素
- 画布上没有对应类型的元素

**解决方案**：
```javascript
// 方案 1：提示用户选择元素
function modifySelected(styles) {
    // 先检查是否有选中元素（可以通过其他方式获知）
    // 如果没有，提示用户
    showNotification('请先选择要修改的元素');
}

// 方案 2：改用其他目标
function modifyAllIfNoneSelected(styles) {
    // 先尝试修改选中的
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'selected',
        styles
    }), '*');
    
    // 如果失败，在响应处理中改为修改所有元素
}

// 方案 3：使用更宽泛的目标
function modifySafely(styles) {
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'all',  // 修改所有元素
        styles
    }), '*');
}
```

### 问题 2：颜色属性不支持相对操作

**症状**：收到 `UNSUPPORTED_OPERATION` 错误

**原因**：尝试对颜色属性使用 `increase`、`decrease` 或 `multiply` 操作

**解决方案**：
```javascript
// ❌ 错误：对颜色使用相对操作
{
    operations: {
        fillColor: { op: 'increase', value: 10 }  // 不支持
    }
}

// ✓ 正确：对颜色使用绝对值
{
    styles: {
        fillColor: '#FF0000'  // 直接设置颜色
    }
}

// ✓ 或者使用 set 操作
{
    operations: {
        fillColor: { op: 'set', value: '#FF0000' }
    }
}
```

### 问题 3：箭头属性应用于非边元素

**症状**：箭头属性被忽略，没有效果

**原因**：箭头属性只对边元素有效

**解决方案**：
```javascript
// ✓ 正确：只对边元素设置箭头
{
    action: 'modifyStyle',
    target: 'edges',  // 只选择边
    styles: {
        endArrow: 'classic',
        endSize: 10
    }
}

// 或者在代码中检查目标类型
function setArrows(target, arrowType) {
    if (target !== 'edges' && target !== 'all') {
        console.warn('箭头属性只对边元素有效');
        return;
    }
    
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target,
        styles: {
            endArrow: arrowType,
            endSize: 10
        }
    }), '*');
}
```

### 问题 4：值超出范围

**症状**：设置的值被自动限制

**原因**：某些属性有值范围限制（如 opacity 0-100）

**解决方案**：
```javascript
// 在发送前验证值范围
function clampValue(property, value) {
    const constraints = {
        opacity: { min: 0, max: 100 },
        fillOpacity: { min: 0, max: 100 },
        strokeOpacity: { min: 0, max: 100 },
        rotation: { min: 0, max: 360 },
        strokeWidth: { min: 0 },
        fontSize: { min: 1 }
    };
    
    const constraint = constraints[property];
    if (!constraint) return value;
    
    if (constraint.min !== undefined && value < constraint.min) {
        return constraint.min;
    }
    if (constraint.max !== undefined && value > constraint.max) {
        return constraint.max;
    }
    
    return value;
}

// 使用示例
const opacity = clampValue('opacity', 150);  // 返回 100
```

### 问题 5：消息发送时机

**症状**：消息发送后没有响应

**原因**：iframe 可能还没有完全加载

**解决方案**：
```javascript
// 等待 iframe 加载完成
const iframe = document.getElementById('drawio-iframe');

iframe.addEventListener('load', function() {
    console.log('iframe 已加载，可以发送消息');
    
    // 现在可以安全地发送消息
    sendStyleMessage({
        action: 'modifyStyle',
        target: 'all',
        styles: { fillColor: '#FF0000' }
    });
});

// 或者使用延迟
setTimeout(() => {
    sendStyleMessage({
        action: 'modifyStyle',
        target: 'all',
        styles: { fillColor: '#FF0000' }
    });
}, 1000);
```

## 性能优化建议

### 1. 批量修改

```javascript
// ✓ 推荐：一次修改多个属性
iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'selected',
    styles: {
        fillColor: '#FF0000',
        strokeColor: '#000000',
        strokeWidth: 3,
        rounded: 1,
        shadow: 1
    }
}), '*');

// ❌ 不推荐：多次单独修改
// 这会触发多次更新，影响性能
iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'selected',
    styles: { fillColor: '#FF0000' }
}), '*');

iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'selected',
    styles: { strokeColor: '#000000' }
}), '*');
```

### 2. 使用精确的目标选择器

```javascript
// ✓ 推荐：精确选择
iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'edges',  // 只修改边
    styles: { strokeWidth: 3 }
}), '*');

// ❌ 不推荐：过于宽泛
iframe.contentWindow.postMessage(JSON.stringify({
    action: 'modifyStyle',
    target: 'all',  // 会处理所有元素，包括不需要的
    styles: { strokeWidth: 3 }
}), '*');
```

### 3. 缓存样式配置

```javascript
// 缓存常用的样式配置
const cachedStyles = {
    darkTheme: {
        action: 'modifyStyle',
        target: 'all',
        styles: {
            fillColor: '#2C2C2C',
            strokeColor: '#FFFFFF',
            fontColor: '#FFFFFF'
        }
    }
};

// 直接使用缓存的配置
function applyDarkTheme() {
    iframe.contentWindow.postMessage(JSON.stringify(cachedStyles.darkTheme), '*');
}
```

### 4. 防抖处理

```javascript
// 防止过于频繁的样式修改
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

// 使用防抖
const debouncedModify = debounce((color) => {
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target: 'selected',
        styles: { fillColor: color }
    }), '*');
}, 300);

// 用户快速改变颜色时，只会发送最后一次
colorPicker.addEventListener('input', (e) => {
    debouncedModify(e.target.value);
});
```

## 安全最佳实践

### 1. 限制允许的 Origin

```javascript
// 生产环境配置
const allowedOrigin = 'https://app.example.com';
const iframeSrc = `https://app.diagrams.net/?embed=1&proto=json&allowedOrigins=${encodeURIComponent(allowedOrigin)}`;

// 在消息监听中验证来源
window.addEventListener('message', function(evt) {
    // 验证消息来源
    if (evt.origin !== 'https://app.diagrams.net') {
        console.warn('拒绝来自未知来源的消息：', evt.origin);
        return;
    }
    
    // 处理消息...
});
```

### 2. 验证用户输入

```javascript
function sanitizeStyleValue(property, value) {
    // 验证颜色值
    if (property.includes('Color')) {
        if (typeof value !== 'string') {
            throw new Error('颜色值必须是字符串');
        }
        
        // 验证十六进制颜色
        if (value.startsWith('#')) {
            const hexPattern = /^#[0-9A-Fa-f]{6}$/;
            if (!hexPattern.test(value)) {
                throw new Error('无效的十六进制颜色值');
            }
        }
    }
    
    // 验证数值
    if (typeof value === 'number') {
        if (!isFinite(value)) {
            throw new Error('无效的数值');
        }
    }
    
    return value;
}

// 使用示例
try {
    const color = sanitizeStyleValue('fillColor', userInput);
    modifyStyle('selected', { fillColor: color });
} catch (error) {
    showError(error.message);
}
```

### 3. 限制操作频率

```javascript
class RateLimiter {
    constructor(maxRequests, timeWindow) {
        this.maxRequests = maxRequests;
        this.timeWindow = timeWindow;
        this.requests = [];
    }
    
    canMakeRequest() {
        const now = Date.now();
        
        // 移除过期的请求记录
        this.requests = this.requests.filter(time => now - time < this.timeWindow);
        
        // 检查是否超过限制
        if (this.requests.length >= this.maxRequests) {
            return false;
        }
        
        this.requests.push(now);
        return true;
    }
}

// 使用示例：每秒最多 5 次请求
const limiter = new RateLimiter(5, 1000);

function modifyStyleWithLimit(target, styles) {
    if (!limiter.canMakeRequest()) {
        console.warn('操作过于频繁，请稍后再试');
        return;
    }
    
    iframe.contentWindow.postMessage(JSON.stringify({
        action: 'modifyStyle',
        target,
        styles
    }), '*');
}
```

## 测试建议

### 单元测试示例

```javascript
describe('Style Modification Integration', () => {
    let iframe;
    
    beforeEach(() => {
        iframe = document.createElement('iframe');
        iframe.src = 'https://app.diagrams.net/?embed=1&proto=json';
        document.body.appendChild(iframe);
    });
    
    afterEach(() => {
        document.body.removeChild(iframe);
    });
    
    test('should send valid message', () => {
        const message = {
            action: 'modifyStyle',
            target: 'selected',
            styles: { fillColor: '#FF0000' }
        };
        
        expect(message.action).toBe('modifyStyle');
        expect(message.target).toBe('selected');
        expect(message.styles.fillColor).toBe('#FF0000');
    });
    
    test('should validate target', () => {
        const validTargets = ['selected', 'edges', 'vertices', 'all'];
        const target = 'selected';
        
        expect(validTargets).toContain(target);
    });
    
    test('should handle response', (done) => {
        window.addEventListener('message', (evt) => {
            const data = JSON.parse(evt.data);
            
            if (data.event === 'modifyStyle') {
                expect(data.status).toBeDefined();
                done();
            }
        });
        
        // 发送消息...
    });
});
```

## 调试技巧

### 1. 启用详细日志

```javascript
const DEBUG = true;

function log(level, message, data) {
    if (!DEBUG) return;
    
    const timestamp = new Date().toISOString();
    console.log(`[${timestamp}] [${level}] ${message}`, data || '');
}

// 使用示例
log('info', '发送样式修改消息', { target: 'selected', styles: { fillColor: '#FF0000' } });
log('success', '操作成功', { modifiedCount: 5 });
log('error', '样式修改失败', { error: 'NO_TARGET_CELLS' });
```

### 2. 消息追踪

```javascript
class MessageTracker {
    constructor() {
        this.messages = [];
    }
    
    track(direction, message) {
        this.messages.push({
            timestamp: Date.now(),
            direction,  // 'sent' or 'received'
            message
        });
    }
    
    getHistory() {
        return this.messages;
    }
    
    clear() {
        this.messages = [];
    }
}

const tracker = new MessageTracker();

// 发送消息时追踪
function sendMessage(message) {
    tracker.track('sent', message);
    iframe.contentWindow.postMessage(JSON.stringify(message), '*');
}

// 接收消息时追踪
window.addEventListener('message', (evt) => {
    const data = JSON.parse(evt.data);
    tracker.track('received', data);
});

// 查看历史
console.log(tracker.getHistory());
```

## 相关资源

- [Draw.io 样式修改能力文档](./DRAWIO_STYLE_CAPABILITIES.md) - 详细的功能说明
- [API 文档](./STYLE_MODIFICATION_API.md) - 完整的 API 规范
- [样式参考](./DRAWIO_STYLE_REFERENCE.md) - 所有可用的样式属性
- [示例代码](../examples/mermaid-integration.html) - 完整的集成示例

## 支持

如有问题或建议，请：
1. 检查浏览器控制台日志
2. 验证消息格式是否正确
3. 确认目标元素是否存在
4. 查看错误代码和错误消息
5. 联系技术支持团队

## 版本信息

- **文档版本**：1.0.0
- **最后更新**：2024-12
- **适用于**：draw.io 版本 22.0.0+
