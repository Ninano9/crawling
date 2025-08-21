package com.newscrawler.service;

import com.newscrawler.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataCleanupService {

    private final ArticleRepository articleRepository;
    
    @Value("${cleanup.keep-days:30}")
    private int keepDays;

    /**
     * 매일 새벽 2시에 오래된 데이터 정리
     * 기본값: 30일 이상 된 기사 삭제
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul")
    @Transactional
    public void cleanupOldArticles() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(keepDays);
        
        try {
            long deletedCount = articleRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("데이터 정리 완료: {}개의 {}일 이상 된 기사 삭제", deletedCount, keepDays);
        } catch (Exception e) {
            log.error("데이터 정리 중 오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 수동 데이터 정리
     */
    @Transactional
    public long manualCleanup(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        try {
            long deletedCount = articleRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("수동 데이터 정리 완료: {}개의 {}일 이상 된 기사 삭제", deletedCount, days);
            return deletedCount;
        } catch (Exception e) {
            log.error("수동 데이터 정리 중 오류 발생: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 데이터베이스 통계 조회
     */
    public String getStorageStats() {
        long totalArticles = articleRepository.count();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long recentArticles = articleRepository.countByCreatedAtAfter(weekAgo);
        
        return String.format(
            "전체 기사: %d개, 최근 7일: %d개, 보관 기간: %d일",
            totalArticles, recentArticles, keepDays
        );
    }
}
