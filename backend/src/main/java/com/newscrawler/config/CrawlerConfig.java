package com.newscrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "crawler")
@Data
public class CrawlerConfig {
    
    private boolean enabled = true;
    private int maxArticlesPerSite = 10;
    private int connectionTimeout = 30000;
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
}
