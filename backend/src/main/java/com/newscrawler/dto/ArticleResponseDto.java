package com.newscrawler.dto;

import com.newscrawler.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponseDto {
    
    private Long id;
    private String title;
    private String summary;
    private String imageUrl;
    private String source;
    private String category;
    private String link;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;

    public static ArticleResponseDto from(Article article) {
        return ArticleResponseDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .imageUrl(article.getImageUrl())
                .source(article.getSource())
                .category(article.getCategory())
                .link(article.getLink())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .build();
    }
}
