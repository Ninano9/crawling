package com.newscrawler.controller;

import com.newscrawler.entity.Article;
import com.newscrawler.service.ArticleService;
import com.newscrawler.service.VideoGenerationService;
import com.newscrawler.service.YouTubeUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoGenerationService videoGenerationService;
    private final YouTubeUploadService youTubeUploadService;
    private final ArticleService articleService;

    /**
     * 특정 기사로 영상 생성 및 YouTube 업로드
     */
    @PostMapping("/generate/{articleId}")
    public ResponseEntity<Map<String, Object>> generateVideoForArticle(@PathVariable Long articleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("기사 ID {}로 영상 생성 요청", articleId);

            // 영상 생성 기능 활성화 확인
            if (!videoGenerationService.isVideoGenerationEnabled()) {
                response.put("success", false);
                response.put("message", "영상 생성 기능이 비활성화되어 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 기사 조회
            Article article = articleService.getArticleById(articleId);
            if (article == null) {
                response.put("success", false);
                response.put("message", "기사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }

            // 영상 생성
            File videoFile = videoGenerationService.generateVideoFromArticle(article);
            
            String youtubeUrl = null;
            if (youTubeUploadService.isUploadEnabled()) {
                // YouTube 업로드
                youtubeUrl = youTubeUploadService.uploadVideo(videoFile, article);
                
                // 로컬 영상 파일 삭제 (업로드 완료 후)
                if (videoFile.exists()) {
                    videoFile.delete();
                }
            }

            response.put("success", true);
            response.put("message", "영상 생성 및 업로드 완료");
            response.put("articleTitle", article.getTitle());
            response.put("videoFile", videoFile.getName());
            response.put("youtubeUrl", youtubeUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("영상 생성 실패", e);
            response.put("success", false);
            response.put("message", "영상 생성 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘의 모든 기사로 영상 생성 (배치)
     */
    @PostMapping("/generate/today")
    public ResponseEntity<Map<String, Object>> generateVideosForTodayArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("오늘의 모든 기사로 영상 생성 요청");

            if (!videoGenerationService.isVideoGenerationEnabled()) {
                response.put("success", false);
                response.put("message", "영상 생성 기능이 비활성화되어 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 오늘의 기사 조회
            List<Article> todayArticles = articleService.getTodayArticles();
            
            if (todayArticles.isEmpty()) {
                response.put("success", false);
                response.put("message", "오늘 수집된 기사가 없습니다.");
                return ResponseEntity.ok(response);
            }

            int successCount = 0;
            int failCount = 0;

            // 각 기사별로 영상 생성
            for (Article article : todayArticles) {
                try {
                    File videoFile = videoGenerationService.generateVideoFromArticle(article);
                    
                    if (youTubeUploadService.isUploadEnabled()) {
                        youTubeUploadService.uploadVideo(videoFile, article);
                        
                        // 업로드 완료 후 로컬 파일 삭제
                        if (videoFile.exists()) {
                            videoFile.delete();
                        }
                    }
                    
                    successCount++;
                    log.info("영상 생성 성공: {}", article.getTitle());
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("영상 생성 실패: {}", article.getTitle(), e);
                }
            }

            response.put("success", true);
            response.put("message", String.format("영상 생성 완료: 성공 %d개, 실패 %d개", successCount, failCount));
            response.put("totalArticles", todayArticles.size());
            response.put("successCount", successCount);
            response.put("failCount", failCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("배치 영상 생성 실패", e);
            response.put("success", false);
            response.put("message", "배치 영상 생성 중 오류 발생: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 영상 생성 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getVideoGenerationStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("videoGenerationEnabled", videoGenerationService.isVideoGenerationEnabled());
        response.put("youtubeUploadEnabled", youTubeUploadService.isUploadEnabled());
        
        return ResponseEntity.ok(response);
    }
}
