package com.newscrawler.controller;

import com.newscrawler.dto.ArticleResponseDto;
import com.newscrawler.dto.ArticlesResponse;
import com.newscrawler.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 오늘의 기사 조회
     * GET /api/articles/today
     */
    @GetMapping("/today")
    public ResponseEntity<ArticlesResponse> getTodaysArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("오늘의 기사 조회 요청 - page: {}, size: {}", page, size);
        
        ArticlesResponse response = size > 0 ? 
                articleService.getTodaysArticles(page, size) : 
                articleService.getTodaysArticles();
        
        return ResponseEntity.ok(response);
    }

    /**
     * 기사 검색
     * GET /api/articles/search?q=검색어
     */
    @GetMapping("/search")
    public ResponseEntity<ArticlesResponse> searchArticles(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("기사 검색 요청 - keyword: {}, page: {}, size: {}", keyword, page, size);
        
        ArticlesResponse response = articleService.searchArticles(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 필터링된 기사 조회
     * GET /api/articles?category=IT&source=네이버뉴스
     */
    @GetMapping
    public ResponseEntity<ArticlesResponse> getFilteredArticles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("필터링된 기사 조회 - category: {}, source: {}, page: {}, size: {}", 
                category, source, page, size);
        
        ArticlesResponse response;
        
        if (category != null && source != null) {
            response = articleService.getArticlesByCategoryAndSource(category, source, page, size);
        } else if (category != null) {
            response = articleService.getArticlesByCategory(category, page, size);
        } else if (source != null) {
            response = articleService.getArticlesBySource(source, page, size);
        } else {
            response = articleService.getTodaysArticles(page, size);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 기간별 기사 조회
     * GET /api/articles/range?start=2024-01-01T00:00:00&end=2024-01-31T23:59:59
     */
    @GetMapping("/range")
    public ResponseEntity<ArticlesResponse> getArticlesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("기간별 기사 조회 - start: {}, end: {}, page: {}, size: {}", start, end, page, size);
        
        ArticlesResponse response = articleService.getArticlesByDateRange(start, end, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 기사 상세 조회
     * GET /api/articles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDto> getArticleById(@PathVariable Long id) {
        log.info("기사 상세 조회 - id: {}", id);
        
        ArticleResponseDto response = articleService.getArticleById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 전체 카테고리 목록 조회
     * GET /api/articles/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("전체 카테고리 목록 조회");
        
        List<String> categories = articleService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * 전체 출처 목록 조회
     * GET /api/articles/sources
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getAllSources() {
        log.info("전체 출처 목록 조회");
        
        List<String> sources = articleService.getAllSources();
        return ResponseEntity.ok(sources);
    }

    /**
     * 출처별 카테고리 목록 조회
     * GET /api/articles/sources/{source}/categories
     */
    @GetMapping("/sources/{source}/categories")
    public ResponseEntity<List<String>> getCategoriesBySource(@PathVariable String source) {
        log.info("출처별 카테고리 목록 조회 - source: {}", source);
        
        List<String> categories = articleService.getCategoriesBySource(source);
        return ResponseEntity.ok(categories);
    }
}
