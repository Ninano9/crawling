package com.newscrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "youtube")
@Data
public class YouTubeConfig {
    
    private String apiKey;
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String channelId;
}
