# 🎬 YouTube API 설정 가이드

## 📋 **필요한 정보들**

YouTube API를 사용하기 위해 다음 5개 값이 필요합니다:

### 1️⃣ **YOUTUBE_API_KEY**
- Google Cloud Console > API 및 서비스 > 사용자 인증 정보
- "사용자 인증 정보 만들기" > "API 키"
- 예시: `AIzaSyDxxxxxxxxxxxxxxxxxxxxxxxxx`

### 2️⃣ **YOUTUBE_CLIENT_ID** 
- Google Cloud Console > API 및 서비스 > 사용자 인증 정보
- "사용자 인증 정보 만들기" > "OAuth 클라이언트 ID"
- 애플리케이션 유형: "데스크톱 애플리케이션"
- 예시: `123456789012-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx.apps.googleusercontent.com`

### 3️⃣ **YOUTUBE_CLIENT_SECRET**
- OAuth 클라이언트 ID 생성 시 함께 제공됨
- 예시: `GOCSPX-xxxxxxxxxxxxxxxxxxxxxxxx`

### 4️⃣ **YOUTUBE_REFRESH_TOKEN**
- OAuth 2.0 Playground 또는 첫 로그인 시 생성됨
- 예시: `1//0Gxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

### 5️⃣ **YOUTUBE_CHANNEL_ID**
- YouTube Studio > 설정 > 채널 > 고급 설정
- 예시: `UCxxxxxxxxxxxxxxxxxxxxxxx`

## 🔧 **설정 방법**

### **로컬 테스트용 (.env 파일)**
```bash
YOUTUBE_API_KEY=여기에_API_키_입력
YOUTUBE_CLIENT_ID=여기에_클라이언트_ID_입력
YOUTUBE_CLIENT_SECRET=여기에_클라이언트_시크릿_입력
YOUTUBE_REFRESH_TOKEN=여기에_리프레시_토큰_입력
YOUTUBE_CHANNEL_ID=여기에_채널_ID_입력
```

### **Render 배포용 (환경 변수)**
Render Dashboard > Environment 탭에서 각각 추가:
- `YOUTUBE_API_KEY` = API 키 값
- `YOUTUBE_CLIENT_ID` = 클라이언트 ID 값
- `YOUTUBE_CLIENT_SECRET` = 클라이언트 시크릿 값  
- `YOUTUBE_REFRESH_TOKEN` = 리프레시 토큰 값
- `YOUTUBE_CHANNEL_ID` = 채널 ID 값

## 🚀 **단계별 설정**

1. **Google Cloud Console** (https://console.cloud.google.com/)에서 새 프로젝트 생성
2. **YouTube Data API v3** 활성화
3. **API 키** 생성 
4. **OAuth 2.0 클라이언트 ID** 생성
5. **YouTube 채널 ID** 확인
6. **환경 변수** 설정

## ⚠️ **보안 주의사항**

- API 키와 시크릿은 **절대 코드에 직접 입력하지 마세요**
- **환경 변수**로만 관리하세요
- GitHub에 업로드하지 마세요 (이미 .gitignore에 포함됨)

## 📞 **도움이 필요하면**

YouTube API 설정에 문제가 있으면 각 단계별로 화면을 확인해서 도와드릴 수 있습니다!
