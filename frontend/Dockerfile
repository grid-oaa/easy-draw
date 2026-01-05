# frontend/Dockerfile
FROM node:20-alpine AS build

WORKDIR /app

# 1. 先拷贝依赖描述，利用缓存
COPY package*.json ./
RUN npm ci

# 2. 再拷贝全部源码（包含 public/drawio）
COPY . .

# 3. Vue CLI 构建（public 目录会原样拷贝进 dist）
RUN npm run build

# 4. 运行阶段：Nginx
FROM nginx:alpine

# 用我们自己的 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 拷贝构建产物
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
