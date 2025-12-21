# Easy Draw 部署文档（Vercel + Render + draw.io）

本文档覆盖：
- 前端 Vue 上线到 Vercel
- 后端 Spring Boot（Jar）上线到 Render
- draw.io 静态站点部署
- 生产环境配置与 CORS 配置

// 后端地址
https://easy-draw.onrender.com/api/ai/demo

// 前端地址
https://easy-draw-bv89kjjsa-grid-oaas-projects.vercel.app/editor

// draw地址
https://drawio-dwy3.onrender.com/

---

## 一、总体架构
- 前端：Vercel（静态托管）
- 后端：Render（Java Web Service，Jar）
- draw.io：单独部署为静态站点（Render Static Site / Vercel / Cloudflare Pages）

生产环境建议三者域名如下：
- 前端：`https://xxx.vercel.app`
- 后端：`https://xxx.onrender.com`
- draw.io：`https://xxx.onrender.com`（静态站点）或 `https://xxx.pages.dev`

---

## 二、后端部署（Render）
1. 推送代码到 GitHub。
2. Render 新建 **Web Service**（Java）。
3. Build Command（Maven）：
   ```
   mvn -f backend/pom.xml clean package -DskipTests
   ```
4. Start Command：
   ```
   java -jar backend/target/*.jar
   ```
5. 部署完成后得到后端域名，如：
   ```
   https://your-backend.onrender.com
   ```

---

## 三、后端 CORS 配置（必须）
后端尚未配置 CORS，需要允许前端域名访问。

新增一个配置类，例如：
`backend/src/main/java/com/easydraw/backend/config/WebCorsConfig.java`

```java
package com.easydraw.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebCorsConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(
            "https://easy-draw.vercel.app"
        )
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
```

> 注意：把 `https://easy-draw.vercel.app` 替换成真实前端域名。

---

## 四、前端部署（Vercel）
1. Vercel 导入 GitHub 仓库。
2. Build Command：
   ```
   npm --prefix frontend install && npm --prefix frontend run build
   ```
3. Output Directory：
   ```
   frontend/dist
   ```
4. 部署完成后得到前端域名：
   ```
   https://easy-draw.vercel.app
   ```

---

## 五、前端 API 配置（生产环境）
当前 `frontend/src/api/http.js` 里 `baseURL` 是固定的 `/api`：

```js
baseURL: '/api'
```

在生产环境可以用两种方式：

### 方案 A（推荐）：Vercel Rewrite
在 Vercel 设置中添加 Rewrite，将 `/api/*` 转发到后端：
```
/api/(.*)  ->  https://your-backend.onrender.com/api/$1
```
这样前端仍然用 `/api`，无需改代码。

### 方案 B：显式配置 baseURL
修改 `http.js`：
```js
baseURL: process.env.VUE_APP_API_BASE_URL || '/api'
```
然后在 Vercel 环境变量中配置：
```
VUE_APP_API_BASE_URL=https://your-backend.onrender.com/api
```

---

## 六、draw.io 部署（必需）
项目依赖本地 draw.io（带 `mermaid-import` 插件）。
建议将 draw.io 源码部署为静态站点。

### 方式 A：Render Static Site（推荐）
1. 把 draw.io 解压目录（如 `drawio-29.2.9`）推到一个独立 Git 仓库。
2. 确保插件文件存在：
   ```
   src/main/webapp/plugins/mermaid-import.js
   ```
3. Render 新建 **Static Site**：
   - Build Command：留空
   - Publish Directory：
     ```
     src/main/webapp
     ```
4. 部署完成得到 draw.io 域名：
   ```
   https://your-drawio.onrender.com
   ```

### 方式 B：Vercel/Cloudflare Pages
同样将 `drawio-29.2.9/src/main/webapp` 作为静态目录发布即可。

---

## 七、前端 draw.io 地址配置
在前端配置 draw.io 基础 URL：

`.env.production` 示例：
```
VUE_APP_DRAWIO_BASE_URL=https://your-drawio.onrender.com
```

然后重新构建部署前端。

---

## 八、生产环境检查清单
- [ ] 后端 Render 服务可访问
- [ ] 前端 Vercel 可访问
- [ ] draw.io 静态站点可访问
- [ ] 前端能成功调用后端 API（无 CORS 报错）
- [ ] Mermaid 可导入 draw.io（插件生效）
- [ ] 模型配置（baseUrl/apiKey/model）能正常使用

---

## 九、常见问题
- **CORS 报错**：确认后端 `allowedOrigins` 配置了前端域名。
- **draw.io 无法导入**：确认使用的是自建 draw.io + 插件版本。
- **API 404**：确认 Vercel Rewrite 或 `VUE_APP_API_BASE_URL` 已正确配置。

