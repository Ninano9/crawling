package com.newscrawler.controller;

import com.newscrawler.service.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final CrawlerService crawlerService;

    /**
     * 수동 크롤링 실행
     * POST /api/crawler/crawl
     */
    @PostMapping("/crawl")
    public ResponseEntity<Map<String, Object>> manualCrawl() {
        log.info("수동 크롤링 요청");
        
        try {
            int savedCount = crawlerService.crawlAllSites();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "크롤링이 완료되었습니다.",
                    "savedCount", savedCount
            ));
        } catch (Exception e) {
            log.error("수동 크롤링 실패: {}", e.getMessage());
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "크롤링 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 특정 소스 크롤링
     * POST /api/crawler/crawl/{source}
     */
    @PostMapping("/crawl/{source}")
    public ResponseEntity<Map<String, Object>> crawlSpecificSource(@PathVariable String source) {
        log.info("특정 소스 크롤링 요청 - source: {}", source);
        
        try {
            var articles = crawlerService.crawlSpecificSource(source);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", source + " 크롤링이 완료되었습니다.",
                    "articleCount", articles.size()
            ));
        } catch (Exception e) {
            log.error("{} 크롤링 실패: {}", source, e.getMessage());
            
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", source + " 크롤링 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 크롤링 통계 조회
     * GET /api/crawler/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCrawlingStats() {
        log.info("크롤링 통계 조회");
        
        String stats = crawlerService.getCrawlingStats();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "stats", stats
        ));
    }

    /**
     * 크롤링 상태 확인
     * GET /api/crawler/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getCrawlerStatus() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "status", "running",
                "message", "크롤러가 정상 동작 중입니다."
        ));
    }
}
