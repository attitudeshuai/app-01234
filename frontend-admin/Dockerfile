# 构建阶段
FROM --platform=$BUILDPLATFORM node:18-alpine AS builder

WORKDIR /app

# 复制依赖文件
COPY package.json ./

# 安装依赖
RUN npm install --registry=https://registry.npmmirror.com

# 复制源代码
COPY . .

# 移除 backend 目录避免构建冲突
RUN rm -rf backend

# 构建项目
RUN npm run build

# 运行阶段
FROM --platform=$TARGETPLATFORM nginx:alpine

# 复制构建产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
