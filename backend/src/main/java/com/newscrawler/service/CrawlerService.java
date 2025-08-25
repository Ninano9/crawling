package com.newscrawler.service;

import com.newscrawler.config.CrawlerConfig;
import com.newscrawler.crawler.NewsCrawler;
import com.newscrawler.entity.Article;
import com.newscrawler.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerService {

    private final NewsCrawler newsCrawler;
    private final ArticleRepository articleRepository;
    private final CrawlerConfig crawlerConfig;
    
    private final Executor executor = Executors.newFixedThreadPool(5);

    /**
     * 매일 오전 7시에 모든 사이트 크롤링 실행
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "Asia/Seoul")
    public void scheduledCrawling() {
        if (!crawlerConfig.isEnabled()) {
            log.info("크롤링이 비활성화되어 있습니다.");
            return;
        }
        
        log.info("===== 일간 뉴스 크롤링 시작 (스케줄러) =====");
        crawlAllSites();
        log.info("===== 일간 뉴스 크롤링 완료 (스케줄러) =====");
    }

    /**
     * 서버 깨우기용 - 매 30분마다 실행
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 30 * 60 * 1000ms
    public void keepServerAwake() {
        log.info("서버 상태 체크 - 현재 시간: {}", java.time.LocalDateTime.now());
        // 단순히 로그만 출력해서 서버가 sleep 모드로 들어가는 것을 방지
    }

    /**
     * 수동 크롤링 실행
     */
    @Transactional
    public int crawlAllSites() {
        List<CompletableFuture<List<Article>>> futures = new ArrayList<>();

        // 다양한 한국 뉴스 사이트 병렬 크롤링
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlNaverNews, executor));     // 한겨레
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlDaumNews, executor));      // 중앙일보
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlZDNetKorea, executor));    // MBC
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlSportsNews, executor));    // SBS
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlEntertainmentNews, executor)); // 연예뉴스
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlEconomyNews, executor));   // 경제뉴스
        futures.add(CompletableFuture.supplyAsync(newsCrawler::crawlSportsNewsExtra, executor)); // 스포츠뉴스

        // 모든 크롤링 완료 대기
        List<Article> allArticles = new ArrayList<>();
        
        for (CompletableFuture<List<Article>> future : futures) {
            try {
                List<Article> articles = future.get();
                allArticles.addAll(articles);
            } catch (Exception e) {
                log.error("크롤링 중 오류 발생: {}", e.getMessage());
            }
        }

        // 중복 제거 및 저장
        int savedCount = 0;
        for (Article article : allArticles) {
            if (!articleRepository.existsByTitleAndSource(article.getTitle(), article.getSource())) {
                try {
                    articleRepository.save(article);
                    savedCount++;
                } catch (Exception e) {
                    log.warn("기사 저장 실패 - 제목: {}, 오류: {}", article.getTitle(), e.getMessage());
                }
            } else {
                log.debug("중복 기사 건너뛰기: {}", article.getTitle());
            }
        }

        log.info("크롤링 완료 - 총 {}개 기사 중 {}개 새로 저장", allArticles.size(), savedCount);
        return savedCount;
    }

    /**
     * 특정 소스만 크롤링
     */
    @Transactional
    public List<Article> crawlSpecificSource(String source) {
        List<Article> articles = new ArrayList<>();
        
        switch (source.toLowerCase()) {
            case "naver", "네이버", "한겨레" -> articles = newsCrawler.crawlNaverNews();
            case "daum", "다음", "중앙일보" -> articles = newsCrawler.crawlDaumNews();
            case "zdnet", "mbc" -> articles = newsCrawler.crawlZDNetKorea();
            case "sports", "스포츠", "sbs" -> articles = newsCrawler.crawlSportsNews();
            case "entertainment", "연예", "스포츠서울" -> articles = newsCrawler.crawlEntertainmentNews();
            case "economy", "경제", "매일경제" -> articles = newsCrawler.crawlEconomyNews();
            case "sportsextra", "스포츠조선" -> articles = newsCrawler.crawlSportsNewsExtra();
            default -> {
                log.warn("지원하지 않는 소스: {}", source);
                return articles;
            }
        }

        // 중복 체크 후 저장
        int savedCount = 0;
        for (Article article : articles) {
            if (!articleRepository.existsByTitleAndSource(article.getTitle(), article.getSource())) {
                articleRepository.save(article);
                savedCount++;
            }
        }

        log.info("{} 크롤링 완료 - 총 {}개 기사 중 {}개 새로 저장", source, articles.size(), savedCount);
        return articles;
    }

    /**
     * 크롤링 통계 조회
     */
    public String getCrawlingStats() {
        long totalArticles = articleRepository.count();
        List<Article> todaysArticles = articleRepository.findTodaysArticles();
        List<String> sources = articleRepository.findDistinctSources();
        
        return String.format(
            "전체 기사: %d개, 오늘 수집: %d개, 수집 소스: %d개 (%s)",
            totalArticles, todaysArticles.size(), sources.size(), String.join(", ", sources)
        );
    }

    /**
     * 특정 출처 기사 삭제
     */
    @Transactional
    public long deleteArticlesBySource(String source) {
        try {
            long deletedCount = articleRepository.deleteBySource(source);
            log.info("{} 출처 기사 삭제 완료: {}개", source, deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("{} 출처 기사 삭제 중 오류: {}", source, e.getMessage());
            throw e;
        }
    }
}
