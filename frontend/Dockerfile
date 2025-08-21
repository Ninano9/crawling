# Multi-stage build for Vue.js application
FROM node:16-alpine AS build

# 작업 디렉토리 설정
WORKDIR /app

# package.json과 yarn.lock 복사 (캐싱 최적화)
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY . .

# 환경 변수 설정
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# 애플리케이션 빌드
RUN npm run build

# 프로덕션 이미지 (Nginx)
FROM nginx:alpine

# Nginx 설정 파일 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 빌드된 정적 파일 복사
COPY --from=build /app/dist /usr/share/nginx/html

# 포트 노출
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
