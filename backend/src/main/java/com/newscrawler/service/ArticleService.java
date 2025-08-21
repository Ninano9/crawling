package com.newscrawler.service;

import com.newscrawler.dto.ArticleResponseDto;
import com.newscrawler.dto.ArticlesResponse;
import com.newscrawler.entity.Article;
import com.newscrawler.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArticleService {

    private final ArticleRepository articleRepository;

    /**
     * 오늘의 기사 조회 (페이징)
     */
    public ArticlesResponse getTodaysArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findTodaysArticles(pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 오늘의 기사 조회 (전체)
     */
    public ArticlesResponse getTodaysArticles() {
        List<Article> articles = articleRepository.findTodaysArticles();
        
        List<ArticleResponseDto> articleDtos = articles.stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(articleDtos, LocalDate.now());
    }

    /**
     * 카테고리별 기사 조회
     */
    public ArticlesResponse getArticlesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findByCategoryOrderByPublishedAtDesc(category, pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 출처별 기사 조회
     */
    public ArticlesResponse getArticlesBySource(String source, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findBySourceOrderByPublishedAtDesc(source, pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 카테고리와 출처로 필터링
     */
    public ArticlesResponse getArticlesByCategoryAndSource(String category, String source, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findByCategoryAndSourceOrderByPublishedAtDesc(category, source, pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 키워드 검색
     */
    public ArticlesResponse searchArticles(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.searchByKeyword(keyword, pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 기간별 기사 조회
     */
    public ArticlesResponse getArticlesByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<Article> articlePage = articleRepository.findByDateRange(startDate, endDate, pageable);
        
        List<ArticleResponseDto> articles = articlePage.getContent()
                .stream()
                .map(ArticleResponseDto::from)
                .collect(Collectors.toList());

        return ArticlesResponse.of(
                articles,
                LocalDate.now(),
                articlePage.getNumber() + 1,
                articlePage.getTotalPages(),
                (int) articlePage.getTotalElements()
        );
    }

    /**
     * 전체 카테고리 목록 조회
     */
    public List<String> getAllCategories() {
        return articleRepository.findDistinctCategories();
    }

    /**
     * 전체 출처 목록 조회
     */
    public List<String> getAllSources() {
        return articleRepository.findDistinctSources();
    }

    /**
     * 출처별 카테고리 목록 조회
     */
    public List<String> getCategoriesBySource(String source) {
        return articleRepository.findDistinctCategoriesBySource(source);
    }

    /**
     * 기사 상세 조회
     */
    public ArticleResponseDto getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("기사를 찾을 수 없습니다: " + id));
        
        return ArticleResponseDto.from(article);
    }
}
