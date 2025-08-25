package com.newscrawler.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tts")
@Data
public class TTSConfig {
    
    private String provider = "edge-tts"; // edge-tts, espeak, piper
    
    private EdgeTTSConfig edge = new EdgeTTSConfig();
    private ESpeakConfig espeak = new ESpeakConfig();
    
    @Data
    public static class EdgeTTSConfig {
        private String voice = "ko-KR-SunHiNeural"; // 한국어 여성 목소리
        private String rate = "+0%"; // 속도 조절
        private String pitch = "+0Hz"; // 피치 조절
    }
    
    @Data
    public static class ESpeakConfig {
        private String voice = "ko";
        private int speed = 175;
        private int pitch = 50;
    }
}
