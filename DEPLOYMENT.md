# 뉴스 크롤링 블로그 - Render 배포 가이드

## 배포 순서

### 1. 백엔드 배포

1. **Render 대시보드**에서 "New Web Service" 선택
2. **GitHub 연결** 후 이 레포지토리 선택
3. **Root Directory**: `backend` 입력
4. **Environment**: Java 선택
5. **Build Command**: `./gradlew bootJar`
6. **Start Command**: `java -jar build/libs/*.jar`

#### 환경 변수 설정:
```
SPRING_PROFILES_ACTIVE=production
PORT=8080
JAVA_OPTS=-Xmx512m -Xms256m
DATABASE_URL=(자동 생성됨)
FRONTEND_URL=https://news-crawler-frontend.onrender.com
CORS_ORIGINS=https://news-crawler-frontend.onrender.com
```

#### 데이터베이스 설정:
- **Type**: PostgreSQL
- **Name**: news-crawler-db
- **Database Name**: newscrawler
- **User**: newscrawler_user

### 2. 프론트엔드 배포

1. **Render 대시보드**에서 "New Static Site" 선택
2. **GitHub 연결** 후 이 레포지토리 선택
3. **Root Directory**: `frontend` 입력
4. **Build Command**: `npm ci && npm run build`
5. **Publish Directory**: `dist`

#### 환경 변수 설정:
```
VITE_API_URL=https://news-crawler-backend.onrender.com
NODE_ENV=production
```

## 배포 후 확인사항

### 백엔드 확인
- Health Check: `https://your-backend-url.onrender.com/actuator/health`
- API 테스트: `https://your-backend-url.onrender.com/api/articles/today`

### 프론트엔드 확인
- 웹사이트 접속: `https://your-frontend-url.onrender.com`
- API 연동 확인
- 크롤링 기능 테스트

## 자동 크롤링 설정

백엔드가 배포된 후, 매일 오전 7시(한국시간)에 자동으로 크롤링이 실행됩니다.

수동 크롤링도 가능합니다:
- 프론트엔드에서 "크롤링 시작" 버튼 클릭
- 또는 API 직접 호출: `POST /api/crawler/crawl`

## 주의사항

1. **Free Tier 제한**:
   - 백엔드: 15분간 비활성시 자동 슬립
   - 데이터베이스: 1GB 저장공간 제한
   - 월 750시간 제한

2. **크롤링 주의**:
   - 대상 사이트의 robots.txt 준수
   - 적절한 요청 간격 유지
   - 과도한 요청으로 인한 IP 차단 방지

3. **성능 최적화**:
   - 이미지 최적화
   - CDN 사용 고려
   - 캐싱 전략 적용

## 커스텀 도메인 설정

Render에서 커스텀 도메인 연결 가능:
1. 도메인 설정에서 CNAME 추가
2. Render 대시보드에서 도메인 연결
3. SSL 인증서 자동 설정

## 모니터링

- Render 대시보드에서 로그 확인
- 메트릭스 모니터링 가능
- 알림 설정 권장
