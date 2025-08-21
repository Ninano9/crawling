# 뉴스 크롤링 블로그 프로젝트

매일 아침 7시에 5개 사이트에서 최신 뉴스를 자동 크롤링하여 블로그 형식으로 제공하는 웹 애플리케이션

## 🚀 기술 스택
- **프론트엔드**: Vue.js 3 (Composition API), Vite, Axios
- **백엔드**: Java Spring Boot, Jsoup, JPA
- **데이터베이스**: PostgreSQL (Production), H2 (Development)
- **배포**: Render

## 📁 프로젝트 구조
```
news-crawler-blog/
├── backend/          # Spring Boot 백엔드
│   ├── src/main/java/com/newscrawler/
│   │   ├── controller/      # REST API 컨트롤러
│   │   ├── service/         # 비즈니스 로직
│   │   ├── repository/      # 데이터 액세스
│   │   ├── entity/          # JPA 엔티티
│   │   ├── dto/             # 데이터 전송 객체
│   │   ├── crawler/         # 크롤링 로직
│   │   └── config/          # 설정 클래스
│   ├── build.gradle
│   ├── Dockerfile
│   └── render.yaml
├── frontend/         # Vue.js 프론트엔드
│   ├── src/
│   │   ├── components/      # Vue 컴포넌트
│   │   ├── views/           # 페이지 뷰
│   │   ├── services/        # API 서비스
│   │   └── utils/           # 유틸리티
│   ├── package.json
│   ├── Dockerfile
│   └── render.yaml
├── README.md
└── DEPLOYMENT.md
```

## ✨ 주요 기능

### 🕷️ 자동 크롤링
- **5개 뉴스 사이트**: 네이버 뉴스, 다음 뉴스, ZDNet 코리아, BBC News, 네이버 스포츠
- **스케줄링**: 매일 오전 7시 자동 크롤링 (한국 시간)
- **병렬 처리**: 5개 사이트 동시 크롤링으로 성능 최적화
- **중복 제거**: 동일한 제목+출처의 기사 중복 방지

### 🎨 사용자 인터페이스
- **반응형 카드 레이아웃**: 모바일/태블릿/데스크톱 지원
- **실시간 검색**: 제목, 요약에서 키워드 검색
- **스마트 필터링**: 카테고리, 출처별 필터링
- **페이지네이션**: 성능 최적화된 페이징

### 🔧 관리 기능
- **수동 크롤링**: 관리자가 원할 때 즉시 크롤링 실행
- **크롤링 통계**: 수집된 기사 수, 출처별 통계
- **상태 모니터링**: 시스템 상태 및 마지막 업데이트 시간

## 🌐 크롤링 대상 사이트

| 사이트 | 카테고리 | 수집 정보 |
|--------|----------|-----------|
| 네이버 뉴스 | IT/과학 | 제목, 요약, 링크 |
| 다음 뉴스 | 디지털 | 제목, 요약, 이미지, 링크 |
| ZDNet 코리아 | IT | 제목, 요약, 이미지, 링크 |
| BBC News | 글로벌 기술 | 제목, 링크 |
| 네이버 스포츠 | 스포츠 | 제목, 링크 |

## 🏃‍♂️ 로컬 개발 환경 설정

### 백엔드 실행
```bash
cd backend
./gradlew bootRun
```

### 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
```

### 환경 변수 설정
```bash
# frontend/env.local
VITE_API_URL=http://localhost:8080
```

## 🚀 배포

자세한 배포 가이드는 [DEPLOYMENT.md](DEPLOYMENT.md)를 참조하세요.

### Render 배포 요약
1. **백엔드**: Web Service (Java) + PostgreSQL Database
2. **프론트엔드**: Static Site (Vue.js)

## 📊 API 엔드포인트

### 기사 API
- `GET /api/articles/today` - 오늘의 기사 조회
- `GET /api/articles/search?q={keyword}` - 기사 검색
- `GET /api/articles?category={}&source={}` - 필터링된 기사 조회
- `GET /api/articles/categories` - 카테고리 목록
- `GET /api/articles/sources` - 출처 목록

### 크롤러 API
- `POST /api/crawler/crawl` - 수동 크롤링 실행
- `GET /api/crawler/stats` - 크롤링 통계
- `GET /api/crawler/status` - 크롤러 상태

## 🔧 개발 상세 정보

### 백엔드 아키텍처
- **Spring Boot 3.2**: 최신 스프링 부트 사용
- **JPA/Hibernate**: 데이터베이스 ORM
- **Jsoup**: HTML 파싱 및 크롤링
- **Spring Scheduler**: 정기적 크롤링 실행
- **Spring Actuator**: 헬스체크 및 모니터링

### 프론트엔드 아키텍처
- **Vue 3 Composition API**: 최신 Vue 문법 사용
- **Vite**: 빠른 개발 서버 및 빌드
- **Axios**: HTTP 클라이언트
- **Date-fns**: 날짜 처리 라이브러리

## 📝 라이센스

MIT License

## 🤝 기여하기

1. 이 저장소를 포크합니다
2. 새로운 기능 브랜치를 생성합니다 (`git checkout -b feature/amazing-feature`)
3. 변경사항을 커밋합니다 (`git commit -m 'Add some amazing feature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/amazing-feature`)
5. 풀 리퀘스트를 생성합니다

## 📞 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 생성해 주세요.
