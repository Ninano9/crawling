package com.newscrawler.repository;

import com.newscrawler.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 오늘 수집된 기사 조회
    @Query("SELECT a FROM Article a WHERE CAST(a.createdAt AS date) = CURRENT_DATE ORDER BY a.publishedAt DESC")
    List<Article> findTodaysArticles();

    // 오늘 수집된 기사 페이징
    @Query("SELECT a FROM Article a WHERE CAST(a.createdAt AS date) = CURRENT_DATE ORDER BY a.publishedAt DESC")
    Page<Article> findTodaysArticles(Pageable pageable);

    // 카테고리별 기사 조회
    Page<Article> findByCategoryOrderByPublishedAtDesc(String category, Pageable pageable);

    // 출처별 기사 조회
    Page<Article> findBySourceOrderByPublishedAtDesc(String source, Pageable pageable);

    // 카테고리와 출처로 필터링
    Page<Article> findByCategoryAndSourceOrderByPublishedAtDesc(String category, String source, Pageable pageable);

    // 제목이나 요약에서 검색
    @Query("SELECT a FROM Article a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(a.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.publishedAt DESC")
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 특정 기간 기사 조회
    @Query("SELECT a FROM Article a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.publishedAt DESC")
    Page<Article> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate, 
                                  Pageable pageable);

    // 중복 기사 체크 (동일한 제목과 출처)
    boolean existsByTitleAndSource(String title, String source);

    // 출처별 카테고리 목록
    @Query("SELECT DISTINCT a.category FROM Article a WHERE a.source = :source ORDER BY a.category")
    List<String> findDistinctCategoriesBySource(@Param("source") String source);

    // 전체 출처 목록
    @Query("SELECT DISTINCT a.source FROM Article a ORDER BY a.source")
    List<String> findDistinctSources();

    // 전체 카테고리 목록
    @Query("SELECT DISTINCT a.category FROM Article a ORDER BY a.category")
    List<String> findDistinctCategories();

    // 데이터 정리용 메서드
    long deleteByCreatedAtBefore(LocalDateTime cutoffDate);
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    // 특정 출처 기사 삭제
    long deleteBySource(String source);
}
