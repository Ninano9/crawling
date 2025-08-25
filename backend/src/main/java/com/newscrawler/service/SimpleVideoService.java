package com.newscrawler.service;

import com.newscrawler.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SimpleVideoService {

    private final TextCleanupService textCleanupService;

    /**
     * 간단한 TTS 테스트 (Edge-TTS 사용)
     */
    public String generateSimpleTTS(String text) {
        try {
            log.info("===== 간단 TTS 테스트 시작 =====");
            
            // 임시 파일 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("simple_tts_%s.mp3", timestamp);
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            File audioFile = tempDir.resolve(fileName).toFile();
            
            // Edge-TTS 명령어 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                "edge-tts",
                "--voice", "ko-KR-SunHiNeural",
                "--text", text,
                "--write-media", audioFile.getAbsolutePath()
            );
            
            log.info("Edge-TTS 명령어 실행 중...");
            Process process = processBuilder.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                return "시간 초과 오류";
            }
            
            if (process.exitValue() == 0) {
                log.info("TTS 성공: {}", audioFile.getName());
                return String.format("TTS 성공! 파일: %s (크기: %d bytes)", 
                    audioFile.getName(), audioFile.length());
            } else {
                return "TTS 실행 실패";
            }
            
        } catch (Exception e) {
            log.error("TTS 테스트 실패", e);
            return "오류: " + e.getMessage();
        }
    }

    /**
     * 기사 내용을 30초 스크립트로 변환 (HTML 정리 포함)
     */
    public String generateScript(Article article) {
        StringBuilder script = new StringBuilder();
        
        // 제목과 요약 HTML 정리
        String cleanTitle = textCleanupService.cleanTitle(article.getTitle());
        String cleanSummary = textCleanupService.cleanSummary(article.getSummary());
        
        // 제목 (5초)
        script.append("오늘의 뉴스입니다. ");
        script.append(cleanTitle).append(". ");
        
        // 요약 내용 (20초)
        if (cleanSummary != null && !cleanSummary.isEmpty()) {
            // 요약을 적절한 길이로 자르기 (약 200자 = 20초)
            if (cleanSummary.length() > 200) {
                cleanSummary = cleanSummary.substring(0, 200) + "...";
            }
            script.append(cleanSummary).append(" ");
        }
        
        // 마무리 (5초)
        script.append("자세한 내용은 ").append(article.getSource()).append("에서 확인하세요.");
        
        // TTS용 최종 정리
        String finalScript = textCleanupService.cleanForTTS(script.toString());
        
        log.info("스크립트 생성 완료: {} 글자", finalScript.length());
        
        return finalScript;
    }
}
