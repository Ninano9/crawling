package com.newscrawler.service;

import com.newscrawler.config.YouTubeConfig;
import com.newscrawler.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeUploadService {

    private final YouTubeConfig youTubeConfig;
    
    /**
     * 영상을 YouTube에 업로드 (현재 비활성화)
     */
    public String uploadVideo(File videoFile, Article article) {
        log.warn("YouTube 업로드 기능이 현재 비활성화되어 있습니다. (의존성 문제로 인해)");
        
        // 임시로 로컬 파일 경로 반환
        String mockUrl = "https://youtube.com/watch?v=mock_" + article.getId();
        log.info("Mock YouTube URL 생성: {}", mockUrl);
        
        return mockUrl;
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
     * YouTube 업로드 가능 여부 확인 (현재는 항상 false)
     */
    public boolean isUploadEnabled() {
        return false; // 현재 비활성화
    }
}
