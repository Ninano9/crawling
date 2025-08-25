package com.newscrawler.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import com.newscrawler.config.YouTubeConfig;
import com.newscrawler.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeUploadService {

    private final YouTubeConfig youTubeConfig;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "News Crawler Video Uploader";

    /**
     * 영상을 YouTube에 업로드
     */
    public String uploadVideo(File videoFile, Article article) {
        try {
            log.info("===== YouTube 업로드 시작: {} =====", article.getTitle());

            // YouTube 서비스 초기화
            YouTube youtubeService = getYouTubeService();

            // 비디오 메타데이터 설정
            Video video = new Video();
            
            // 비디오 정보 설정
            VideoSnippet snippet = new VideoSnippet();
            snippet.setTitle(generateVideoTitle(article));
            snippet.setDescription(generateVideoDescription(article));
            snippet.setTags(Arrays.asList("뉴스", "한국뉴스", "뉴스요약", article.getSource(), "shorts"));
            snippet.setCategoryId("25"); // News & Politics 카테고리
            snippet.setDefaultLanguage("ko");
            snippet.setDefaultAudioLanguage("ko");
            
            video.setSnippet(snippet);

            // 비디오 상태 설정 (공개/비공개)
            VideoStatus status = new VideoStatus();
            status.setPrivacyStatus("public"); // public, unlisted, private
            status.setSelfDeclaredMadeForKids(false);
            video.setStatus(status);

            // 파일 업로드 스트림 생성
            InputStreamContent mediaContent = new InputStreamContent(
                "video/mp4", 
                new FileInputStream(videoFile)
            );
            mediaContent.setLength(videoFile.length());

            // YouTube 업로드 요청
            YouTube.Videos.Insert videoInsert = youtubeService.videos()
                .insert(Arrays.asList("snippet", "status"), video, mediaContent);

            // 업로드 실행
            Video uploadedVideo = videoInsert.execute();
            String videoId = uploadedVideo.getId();
            String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

            log.info("===== YouTube 업로드 완료: {} =====", videoUrl);
            return videoUrl;

        } catch (Exception e) {
            log.error("YouTube 업로드 실패: {}", article.getTitle(), e);
            throw new RuntimeException("YouTube 업로드 중 오류 발생", e);
        }
    }

    /**
     * YouTube 서비스 초기화
     */
    private YouTube getYouTubeService() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // OAuth2 인증 정보로 Credential 생성
        Credential credential = createCredential();
        
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    /**
     * OAuth2 Credential 생성
     */
    private Credential createCredential() {
        try {
            // Refresh Token을 사용한 인증
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setRefreshToken(youTubeConfig.getRefreshToken());
            
            GoogleCredential credential = new GoogleCredential.Builder()
                .setClientSecrets(youTubeConfig.getClientId(), youTubeConfig.getClientSecret())
                .setJsonFactory(JSON_FACTORY)
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .build();
            
            credential.setFromTokenResponse(tokenResponse);
            credential.refreshToken();
            
            return credential;
            
        } catch (Exception e) {
            log.error("YouTube 인증 실패", e);
            throw new RuntimeException("YouTube 인증 오류", e);
        }
    }

    /**
     * 영상 제목 생성
     */
    private String generateVideoTitle(Article article) {
        String title = article.getTitle();
        
        // 제목이 너무 길면 자르기 (YouTube 제목 최대 100자)
        if (title.length() > 90) {
            title = title.substring(0, 87) + "...";
        }
        
        return "[뉴스요약] " + title + " #Shorts";
    }

    /**
     * 영상 설명 생성
     */
    private String generateVideoDescription(Article article) {
        StringBuilder description = new StringBuilder();
        
        description.append("📰 오늘의 뉴스를 30초로 요약했습니다!\n\n");
        description.append("📌 제목: ").append(article.getTitle()).append("\n");
        description.append("📅 발행일: ").append(article.getPublishedAt()).append("\n");
        description.append("📰 출처: ").append(article.getSource()).append("\n\n");
        
        if (article.getSummary() != null && !article.getSummary().isEmpty()) {
            description.append("📝 요약:\n").append(article.getSummary()).append("\n\n");
        }
        
        if (article.getLink() != null && !article.getLink().isEmpty()) {
            description.append("🔗 원문 링크: ").append(article.getLink()).append("\n\n");
        }
        
        description.append("🤖 이 영상은 자동으로 생성되었습니다.\n");
        description.append("#뉴스 #한국뉴스 #뉴스요약 #Shorts #").append(article.getSource().replace(" ", ""));
        
        return description.toString();
    }

    /**
     * YouTube 업로드 가능 여부 확인
     */
    public boolean isUploadEnabled() {
        return youTubeConfig.getApiKey() != null && 
               youTubeConfig.getClientId() != null && 
               youTubeConfig.getClientSecret() != null && 
               youTubeConfig.getRefreshToken() != null;
    }
}
