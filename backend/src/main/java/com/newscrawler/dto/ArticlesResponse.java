package com.newscrawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticlesResponse {
    
    private LocalDate date;
    private String message;
    private int totalCount;
    private int currentPage;
    private int totalPages;
    private List<ArticleResponseDto> articles;
    
    public static ArticlesResponse of(List<ArticleResponseDto> articles, LocalDate date) {
        return ArticlesResponse.builder()
                .date(date)
                .message("기사를 성공적으로 조회했습니다.")
                .totalCount(articles.size())
                .currentPage(1)
                .totalPages(1)
                .articles(articles)
                .build();
    }
    
    public static ArticlesResponse of(List<ArticleResponseDto> articles, LocalDate date, 
                                    int currentPage, int totalPages, int totalCount) {
        return ArticlesResponse.builder()
                .date(date)
                .message("기사를 성공적으로 조회했습니다.")
                .totalCount(totalCount)
                .currentPage(currentPage)
                .totalPages(totalPages)
                .articles(articles)
                .build();
    }
}
