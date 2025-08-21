# 개발 가이드

## 🛠️ 로컬 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Node.js 16 이상
- Git

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd crawling
```

### 2. 백엔드 실행

#### H2 데이터베이스로 실행 (개발용)
```bash
cd backend
./gradlew bootRun
```

#### PostgreSQL로 실행 (프로덕션 유사 환경)
```bash
# PostgreSQL 설치 및 데이터베이스 생성
createdb newscrawler

# application.yml 설정 변경 또는 환경변수 설정
export SPRING_PROFILES_ACTIVE=dev

cd backend
./gradlew bootRun
```

백엔드 서버: http://localhost:8080

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

프론트엔드 서버: http://localhost:5173

### 4. H2 콘솔 접속 (개발용)
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (비어있음)

## 🧪 테스트

### 백엔드 API 테스트

#### 1. 수동 크롤링 실행
```bash
curl -X POST http://localhost:8080/api/crawler/crawl
```

#### 2. 기사 조회
```bash
# 오늘의 기사
curl http://localhost:8080/api/articles/today

# 검색
curl "http://localhost:8080/api/articles/search?q=AI"

# 카테고리별
curl "http://localhost:8080/api/articles?category=IT"
```

#### 3. 헬스체크
```bash
curl http://localhost:8080/actuator/health
```

### 프론트엔드 기능 테스트

1. **홈페이지 접속**: http://localhost:5173
2. **크롤링 테스트**: "크롤링 시작" 버튼 클릭
3. **검색 테스트**: 상단 검색창에 키워드 입력
4. **필터 테스트**: 카테고리/출처 필터 클릭
5. **기사 링크**: 카드 클릭시 원본 기사 새창으로 열기

## 🔧 개발 팁

### 백엔드

#### 로그 레벨 조정
```yaml
# application.yml
logging:
  level:
    com.newscrawler: DEBUG
```

#### 크롤링 비활성화 (개발시)
```yaml
# application.yml
crawler:
  enabled: false
```

#### 스케줄러 테스트
```java
// CrawlerService.java에 추가
@EventListener(ApplicationReadyEvent.class)
public void testCrawling() {
    if (crawlerConfig.isEnabled()) {
        log.info("애플리케이션 시작시 크롤링 테스트 실행");
        crawlAllSites();
    }
}
```

### 프론트엔드

#### 환경변수 설정
```bash
# frontend/.env.local 생성
VITE_API_URL=http://localhost:8080
```

#### API 호출 디버깅
브라우저 개발자 도구 Network 탭에서 API 요청/응답 확인

#### Hot Reload
코드 변경시 자동으로 브라우저 새로고침

## 🚀 빌드 및 배포

### 로컬 빌드 테스트

#### 백엔드
```bash
cd backend
./gradlew build
java -jar build/libs/*.jar
```

#### 프론트엔드
```bash
cd frontend
npm run build
npm run preview
```

### Docker 빌드 테스트

#### 백엔드
```bash
cd backend
docker build -t news-crawler-backend .
docker run -p 8080:8080 news-crawler-backend
```

#### 프론트엔드
```bash
cd frontend
docker build -t news-crawler-frontend --build-arg VITE_API_URL=http://localhost:8080 .
docker run -p 80:80 news-crawler-frontend
```

## 🐛 트러블슈팅

### 자주 발생하는 문제

#### 1. 크롤링 실패
- **원인**: 대상 사이트 HTML 구조 변경
- **해결**: NewsCrawler.java에서 CSS 셀렉터 업데이트

#### 2. CORS 오류
- **원인**: 프론트엔드-백엔드 간 CORS 정책
- **해결**: WebConfig.java에서 allowedOrigins 확인

#### 3. 데이터베이스 연결 실패
- **원인**: PostgreSQL 서비스 미실행 또는 잘못된 연결 정보
- **해결**: application.yml의 datasource 설정 확인

#### 4. 의존성 오류
```bash
# 백엔드
cd backend
./gradlew clean build

# 프론트엔드
cd frontend
rm -rf node_modules
npm install
```

### 로그 확인

#### 백엔드 로그
- 콘솔 출력 확인
- Spring Boot Actuator: http://localhost:8080/actuator/loggers

#### 프론트엔드 로그
- 브라우저 개발자 도구 Console 탭
- Network 탭에서 API 요청 상태 확인

## 📈 성능 최적화

### 백엔드
- 데이터베이스 인덱스 추가
- 크롤링 병렬 처리 최적화
- 캐싱 전략 적용

### 프론트엔드
- 이미지 lazy loading
- 가상 스크롤링 적용
- 번들 크기 최적화

## 🔐 보안 고려사항

1. **크롤링 에티켓**
   - robots.txt 준수
   - 적절한 User-Agent 설정
   - 요청 간격 조절

2. **API 보안**
   - 필요시 인증/인가 추가
   - 입력값 검증
   - SQL 인젝션 방지

3. **배포 보안**
   - 환경변수로 민감정보 관리
   - HTTPS 사용
   - 보안 헤더 설정
