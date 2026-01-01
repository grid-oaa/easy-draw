# 代码风格与约定

## 后端（Java）
- 包名：com.easydraw.backend.*
- 代码风格：标准 Java Bean（显式 getter/setter），无 Lombok。
- 注释与文案：多为中文。
- 日志：SLF4J LoggerFactory。
- 依赖注入：构造器注入为主。

## 前端（Vue 2）
- JavaScript + Vue 2 Options API。
- ESLint + Prettier（package.json scripts）。