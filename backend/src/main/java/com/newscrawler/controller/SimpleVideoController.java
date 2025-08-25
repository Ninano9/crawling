package com.newscrawler.controller;

import com.newscrawler.entity.Article;
import com.newscrawler.service.ArticleService;
import com.newscrawler.service.SimpleVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
