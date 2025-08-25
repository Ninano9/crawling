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

    /**
     * 완전한 영상 생성 (음성 + 이미지 → MP4)
     */
    public String generateCompleteVideo(Article article) {
        try {
            log.info("===== 완전한 영상 생성 시작: {} =====", article.getTitle());
            
            // 1. 스크립트 생성
            String script = generateScript(article);
            log.info("스크립트 생성 완료: {} 글자", script.length());
            
            // 2. TTS 음성 생성
            File audioFile = generateAudioFile(script);
            log.info("음성 파일 생성 완료: {}", audioFile.getName());
            
            // 3. 이미지 준비
            File imageFile = prepareImageFile(article);
            log.info("이미지 파일 준비 완료: {}", imageFile.getName());
            
            // 4. 영상 생성 (FFmpeg)
            File videoFile = createVideoWithFFmpeg(audioFile, imageFile, article);
            log.info("영상 생성 완료: {} (크기: {}bytes)", videoFile.getName(), videoFile.length());
            
            // 5. 임시 파일 정리
            if (audioFile.exists()) audioFile.delete();
            if (imageFile.exists()) imageFile.delete();
            
            return String.format("영상 생성 성공! 파일: %s (크기: %d bytes)", 
                videoFile.getName(), videoFile.length());
                
        } catch (Exception e) {
            log.error("완전한 영상 생성 실패", e);
            return "영상 생성 실패: " + e.getMessage();
        }
    }

    /**
     * TTS로 음성 파일 생성
     */
    private File generateAudioFile(String script) throws Exception {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("video_audio_%s.mp3", timestamp);
        java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));
        File audioFile = tempDir.resolve(fileName).toFile();
        
        // Edge-TTS 명령어 실행
        ProcessBuilder processBuilder = new ProcessBuilder(
            "edge-tts",
            "--voice", "ko-KR-SunHiNeural",
            "--text", script,
            "--write-media", audioFile.getAbsolutePath()
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("TTS 실행 시간 초과");
        }
        
        if (process.exitValue() != 0) {
            throw new RuntimeException("TTS 실행 실패");
        }
        
        return audioFile;
    }

    /**
     * 기사 이미지 파일 준비
     */
    private File prepareImageFile(Article article) throws Exception {
        String imageUrl = article.getImageUrl();
        
        // 이미지 URL이 있으면 다운로드 시도
        if (imageUrl != null && imageUrl.startsWith("http")) {
            try {
                return downloadImage(imageUrl, article.getId());
            } catch (Exception e) {
                log.warn("이미지 다운로드 실패, 기본 이미지 생성: {}", e.getMessage());
            }
        }
        
        // 기본 이미지 생성
        return createDefaultImage(article.getId());
    }

    /**
     * 이미지 다운로드
     */
    private File downloadImage(String imageUrl, Long articleId) throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));
        String fileName = String.format("news_image_%s.jpg", articleId);
        File imageFile = tempDir.resolve(fileName).toFile();
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "curl", "-L", "-o", imageFile.getAbsolutePath(), imageUrl
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("이미지 다운로드 시간 초과");
        }
        
        if (process.exitValue() == 0 && imageFile.exists() && imageFile.length() > 0) {
            return imageFile;
        }
        
        throw new RuntimeException("이미지 다운로드 실패");
    }

    /**
     * 기본 이미지 생성 (파란색 배경)
     */
    private File createDefaultImage(Long articleId) throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Paths.get(System.getProperty("java.io.tmpdir"));
        String fileName = String.format("default_bg_%s.jpg", articleId);
        File imageFile = tempDir.resolve(fileName).toFile();
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "ffmpeg", "-y",
            "-f", "lavfi",
            "-i", "color=c=blue:size=1080x1920:duration=1",
            "-frames:v", "1",
            imageFile.getAbsolutePath()
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("기본 이미지 생성 시간 초과");
        }
        
        if (process.exitValue() == 0 && imageFile.exists()) {
            return imageFile;
        }
        
        throw new RuntimeException("기본 이미지 생성 실패");
    }

    /**
     * FFmpeg로 영상 생성
     */
    private File createVideoWithFFmpeg(File audioFile, File imageFile, Article article) throws Exception {
        java.nio.file.Path outputDir = java.nio.file.Paths.get("./videos");
        if (!java.nio.file.Files.exists(outputDir)) {
            java.nio.file.Files.createDirectories(outputDir);
        }
        
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("news_%s_%s.mp4", article.getId(), timestamp);
        File videoFile = outputDir.resolve(fileName).toFile();
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "ffmpeg", "-y",
            "-loop", "1",
            "-i", imageFile.getAbsolutePath(),
            "-i", audioFile.getAbsolutePath(),
            "-c:v", "libx264",
            "-tune", "stillimage",
            "-c:a", "aac",
            "-b:a", "192k",
            "-pix_fmt", "yuv420p",
            "-vf", "scale=1080:1920:force_original_aspect_ratio=increase,crop=1080:1920",
            "-r", "30",
            "-shortest",
            "-fflags", "+shortest",
            "-max_interleave_delta", "100M",
            videoFile.getAbsolutePath()
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(120, TimeUnit.SECONDS); // 2분 타임아웃
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("영상 생성 시간 초과");
        }
        
        if (process.exitValue() != 0) {
            throw new RuntimeException("FFmpeg 영상 생성 실패");
        }
        
        return videoFile;
    }
}
