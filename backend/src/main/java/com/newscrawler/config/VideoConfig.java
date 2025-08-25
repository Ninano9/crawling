package com.newscrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "video.generation")
@Data
public class VideoConfig {
    
    private boolean enabled = false;
    private String outputDirectory = "./videos";
    private int maxDurationSeconds = 30;
    private String resolution = "1080x1920"; // 세로형 (숏폼)
    private int frameRate = 30;
}
