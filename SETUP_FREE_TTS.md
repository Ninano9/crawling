# 🆓 무료 TTS 설치 가이드

## 📋 개요
Google Cloud 없이 **완전 무료**로 TTS(Text-to-Speech)를 사용하여 YouTube 숏폼 영상을 생성하는 방법입니다.

## 🎯 추천 옵션: Edge-TTS (Microsoft)

### ✅ 장점
- **완전 무료, 제한 없음**
- **한국어 음질 매우 좋음** (Microsoft의 고품질 TTS)
- **설치 간단**
- **다양한 목소리 지원**

### 🛠 설치 방법

#### 1️⃣ Python 설치 (Windows)
```bash
# Python 3.8 이상 필요
# https://www.python.org/downloads/ 에서 다운로드
```

#### 2️⃣ Edge-TTS 설치
```bash
pip install edge-tts
```

#### 3️⃣ 테스트
```bash
# 사용 가능한 한국어 목소리 확인
edge-tts --list-voices | grep ko-KR

# 테스트 음성 생성
edge-tts --voice "ko-KR-SunHiNeural" --text "안녕하세요, 테스트입니다." --write-media test.mp3
```

### 🎤 한국어 목소리 옵션
- `ko-KR-SunHiNeural` - 여성 (추천)
- `ko-KR-InJoonNeural` - 남성
- `ko-KR-BongJinNeural` - 남성
- `ko-KR-GookMinNeural` - 남성
- `ko-KR-JiMinNeural` - 여성
- `ko-KR-SeoHyeonNeural` - 여성
- `ko-KR-SoonBokNeural` - 여성
- `ko-KR-YuJinNeural` - 여성

## 🔧 대안 옵션: eSpeak (오픈소스)

### 🛠 설치 방법

#### Windows
```bash
# Chocolatey로 설치
choco install espeak

# 또는 직접 다운로드
# http://espeak.sourceforge.net/download.html
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install espeak espeak-data
```

#### 테스트
```bash
# 한국어 테스트
espeak -v ko "안녕하세요" -w test.wav
```

## 🎬 FFmpeg 설치 (영상 생성용)

### Windows
```bash
# Chocolatey로 설치
choco install ffmpeg

# 또는 직접 다운로드
# https://ffmpeg.org/download.html#build-windows
```

### Linux
```bash
sudo apt-get install ffmpeg
```

### 테스트
```bash
ffmpeg -version
```

## 🚀 Render 배포 설정

### 환경변수 설정
```bash
# TTS 설정
TTS_PROVIDER=edge-tts
EDGE_TTS_VOICE=ko-KR-SunHiNeural
EDGE_TTS_RATE=+0%
EDGE_TTS_PITCH=+0Hz

# 영상 생성 활성화
VIDEO_GENERATION_ENABLED=true

# YouTube 설정 (기존과 동일)
YOUTUBE_API_KEY=your-api-key
YOUTUBE_CLIENT_ID=your-client-id
YOUTUBE_CLIENT_SECRET=your-client-secret
YOUTUBE_REFRESH_TOKEN=your-refresh-token
YOUTUBE_CHANNEL_ID=your-channel-id
```

### Dockerfile 수정 (Render용)
```dockerfile
# Python과 FFmpeg 설치 추가
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    ffmpeg \
    espeak \
    && rm -rf /var/lib/apt/lists/*

# Edge-TTS 설치
RUN pip3 install edge-tts
```

## 📊 비용 비교

| 서비스 | 비용 | 제한 | 음질 |
|--------|------|------|------|
| **Edge-TTS** | **무료** | **없음** | **⭐⭐⭐⭐⭐** |
| Google Cloud TTS | $4/1M 글자 | 크레딧 소진시 과금 | ⭐⭐⭐⭐⭐ |
| eSpeak | 무료 | 없음 | ⭐⭐⭐ |

## 🎯 사용법

### API 호출
```bash
# 특정 기사로 영상 생성
curl -X POST https://crawling-backend.onrender.com/api/video/generate/123

# 오늘 모든 기사 영상화
curl -X POST https://crawling-backend.onrender.com/api/video/generate/today

# 상태 확인
curl https://crawling-backend.onrender.com/api/video/status
```

### 로그 확인
```
===== 무료 TTS 음성 생성 시작: 150 글자 =====
Edge-TTS로 음성 생성 중...
Edge-TTS 음성 생성 완료: tts_audio_20250825_143022.mp3
===== 영상 생성 시작: [뉴스제목] =====
영상 생성 완료: news_123_20250825_143025.mp4
===== YouTube 업로드 완료: https://www.youtube.com/watch?v=... =====
```

## 🔧 문제 해결

### Edge-TTS 실패시
- eSpeak으로 자동 대체
- 무음 파일 생성 (최후 수단)

### 권한 오류
```bash
# Python 경로 확인
which python3
python3 --version

# 권한 설정
chmod +x /usr/bin/python3
```

## ✅ 완료!

이제 **Google Cloud 없이 완전 무료**로 뉴스 기사를 YouTube 숏폼 영상으로 자동 생성할 수 있습니다! 🎬✨
