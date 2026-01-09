# Easy Draw

 :rocket:**在线体验地址**：http://123.57.223.239/draw/（模型key只会记录在本地浏览器的localstorage）

Easy Draw 是 AI + draw.io 的在线绘图原型。
使用自然语言描述业务 / 修改图形，生成drawio可编辑图形、Mermaid格式图形。
默认设置了多套绘图风格，图形可保存为png或drawio格式
![系统界面.png](doc/assets/%E7%B3%BB%E7%BB%9F%E7%95%8C%E9%9D%A2.png)

## 功能亮点
- 文本生成图表：自然语言生成 Mermaid 图表代码
- 历史数据导入/插入：将历史图形导入 draw.io 画布
- **样式修改能力：AI 指令 + 内置风格预设**
- 本地保存与导出：支持 .drawio / PNG
- 模型可配置：Base URL / API Key / Model

![展示.gif](doc/assets/%E5%B1%95%E7%A4%BA.gif)

## 样式修改能力（核心特色）
选中“修改”按钮后，使用对话的方式修改图形的颜色、线条、透明度、字体等等属性
![样式修改.png](doc/assets/%E6%A0%B7%E5%BC%8F%E4%BF%AE%E6%94%B9.png)


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

## 快速开始
### 1) 二开后的draw.io（开源地址：[GitHub - grid-oaa/drawio](https://github.com/grid-oaa/drawio)）
- 该资源已放在 frontend/public/drawio
- 设置 VUE_APP_DRAWIO_BASE_URL=/drawio
### 2) 启动后端
```
cd backend
mvn spring-boot:run
```
默认端口 8081，默认上下文路径为 /drawio。
### 3) 启动前端
```
cd frontend
npm install
npm run serve
```
### 4) 首次使用
- 右侧点击 Model 填写 Base URL / API Key / Model
- 输入描述生成图表 / 修改样式
