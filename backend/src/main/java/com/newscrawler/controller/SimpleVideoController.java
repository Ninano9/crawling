package com.newscrawler.controller;

import com.newscrawler.entity.Article;
import com.newscrawler.service.ArticleService;
import com.newscrawler.service.SimpleVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple-video")
@RequiredArgsConstructor
@Slf4j
public class SimpleVideoController {

    private final SimpleVideoService simpleVideoService;
    private final ArticleService articleService;

    /**
     * 간단한 TTS 테스트
     */
    @PostMapping("/test-tts")
    public ResponseEntity<Map<String, Object>> testTTS(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String text = request.getOrDefault("text", "안녕하세요! 무료 TTS 테스트입니다.");
            
            log.info("TTS 테스트 요청: {}", text);
            String result = simpleVideoService.generateSimpleTTS(text);
            
            response.put("success", true);
            response.put("message", "TTS 테스트 완료");
            response.put("result", result);
            response.put("inputText", text);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("TTS 테스트 실패", e);
            response.put("success", false);
            response.put("message", "TTS 테스트 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 기사로 스크립트 생성 테스트
     */
    @GetMapping("/script/{articleId}")
    public ResponseEntity<Map<String, Object>> generateScript(@PathVariable Long articleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("기사 ID {}로 스크립트 생성 요청", articleId);

            // 기사 조회
            Article article = articleService.getArticleById(articleId);
            
            // 스크립트 생성
            String script = simpleVideoService.generateScript(article);
            
            response.put("success", true);
            response.put("message", "스크립트 생성 완료");
            response.put("articleTitle", article.getTitle());
            response.put("script", script);
            response.put("scriptLength", script.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("스크립트 생성 실패", e);
            response.put("success", false);
            response.put("message", "스크립트 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 기사 → 스크립트 → TTS 전체 테스트
     */
    @PostMapping("/full-test/{articleId}")
    public ResponseEntity<Map<String, Object>> fullTest(@PathVariable Long articleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("기사 ID {}로 전체 테스트 요청", articleId);

            // 1. 기사 조회
            Article article = articleService.getArticleById(articleId);
            
            // 2. 스크립트 생성
            String script = simpleVideoService.generateScript(article);
            
            // 3. TTS 생성
            String ttsResult = simpleVideoService.generateSimpleTTS(script);
            
            response.put("success", true);
            response.put("message", "전체 테스트 완료");
            response.put("articleTitle", article.getTitle());
            response.put("script", script);
            response.put("scriptLength", script.length());
            response.put("ttsResult", ttsResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("전체 테스트 실패", e);
            response.put("success", false);
            response.put("message", "전체 테스트 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘 기사 목록 조회 (테스트용)
     */
    @GetMapping("/today-articles")
    public ResponseEntity<Map<String, Object>> getTodayArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Article> articles = articleService.getTodayArticles();
            
            response.put("success", true);
            response.put("message", "오늘 기사 조회 완료");
            response.put("count", articles.size());
            response.put("articles", articles.stream()
                .limit(5) // 처음 5개만
                .map(article -> Map.of(
                    "id", article.getId(),
                    "title", article.getTitle(),
                    "source", article.getSource()
                ))
                .toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("오늘 기사 조회 실패", e);
            response.put("success", false);
            response.put("message", "오늘 기사 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 완전한 영상 생성 테스트 (기사 → 스크립트 → TTS → 영상)
     */
    @PostMapping("/generate-video/{articleId}")
    public ResponseEntity<Map<String, Object>> generateCompleteVideo(@PathVariable Long articleId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("기사 ID {}로 완전한 영상 생성 요청", articleId);

            // 기사 조회
            Article article = articleService.getArticleById(articleId);
            
            // 완전한 영상 생성 (음성 + 이미지 → MP4)
            String videoResult = simpleVideoService.generateCompleteVideo(article);
            
            response.put("success", true);
            response.put("message", "완전한 영상 생성 완료");
            response.put("articleTitle", article.getTitle());
            response.put("videoResult", videoResult);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("완전한 영상 생성 실패", e);
            response.put("success", false);
            response.put("message", "완전한 영상 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘의 모든 기사로 영상 생성 (배치)
     */
    @PostMapping("/generate-videos/today")
    public ResponseEntity<Map<String, Object>> generateVideosForTodayArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("오늘의 모든 기사로 영상 생성 요청");

            List<Article> todayArticles = articleService.getTodayArticles();
            
            if (todayArticles.isEmpty()) {
                response.put("success", false);
                response.put("message", "오늘 수집된 기사가 없습니다.");
                return ResponseEntity.ok(response);
            }

            int successCount = 0;
            int failCount = 0;

            // 처음 3개 기사만 테스트 (시간 단축)
            for (Article article : todayArticles.stream().limit(3).toList()) {
                try {
                    String videoResult = simpleVideoService.generateCompleteVideo(article);
                    successCount++;
                    log.info("영상 생성 성공: {} - {}", article.getTitle(), videoResult);
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("영상 생성 실패: {}", article.getTitle(), e);
                }
            }

            response.put("success", true);
            response.put("message", String.format("영상 생성 완료: 성공 %d개, 실패 %d개", successCount, failCount));
            response.put("totalArticles", Math.min(3, todayArticles.size()));
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
     * 생성된 파일 목록 조회
     */
    @GetMapping("/files")
    public ResponseEntity<Map<String, Object>> listGeneratedFiles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Path videosDir = Paths.get("./videos");
            
            if (!Files.exists(videosDir)) {
                response.put("success", true);
                response.put("message", "생성된 파일이 없습니다.");
                response.put("files", List.of());
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> fileList = Files.list(videosDir)
                .filter(Files::isRegularFile)
                .map(file -> {
                    try {
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("name", file.getFileName().toString());
                        fileInfo.put("size", Files.size(file));
                        fileInfo.put("modified", Files.getLastModifiedTime(file).toString());
                        return fileInfo;
                    } catch (Exception e) {
                        Map<String, Object> errorInfo = new HashMap<>();
                        errorInfo.put("name", file.getFileName().toString());
                        errorInfo.put("error", e.getMessage());
                        return errorInfo;
                    }
                })
                .toList();

            response.put("success", true);
            response.put("message", "파일 목록 조회 완료");
            response.put("count", fileList.size());
            response.put("files", fileList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("파일 목록 조회 실패", e);
            response.put("success", false);
            response.put("message", "파일 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 파일 다운로드
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("./videos").resolve(fileName).normalize();
            
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath.toFile());
            
            // 파일 확장자에 따른 Content-Type 설정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                if (fileName.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else if (fileName.endsWith(".mp3")) {
                    contentType = "audio/mpeg";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(resource);

        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
