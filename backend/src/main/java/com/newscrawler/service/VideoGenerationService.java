package com.newscrawler.service;

import com.newscrawler.config.VideoConfig;
import com.newscrawler.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
// JAVE 라이브러리 대신 FFmpeg 직접 사용

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoGenerationService {

    private final VideoConfig videoConfig;
    private final TTSService ttsService;

    /**
     * 기사를 30초 영상으로 변환
     */
    public File generateVideoFromArticle(Article article) {
        try {
            log.info("===== 영상 생성 시작: {} =====", article.getTitle());

            // 1. 스크립트 생성 (30초 분량)
            String script = generateScript(article);
            log.info("스크립트 생성 완료: {} 글자", script.length());

            // 2. TTS로 음성 생성
            File audioFile = ttsService.generateAudio(script);
            log.info("음성 파일 생성 완료: {}", audioFile.getName());

            // 3. 영상 생성 (음성 + 이미지)
            File videoFile = createVideoWithAudioAndImage(audioFile, article);
            log.info("영상 생성 완료: {}", videoFile.getName());

            // 4. 임시 파일 정리
            if (audioFile.exists()) {
                audioFile.delete();
            }

            return videoFile;

        } catch (Exception e) {
            log.error("영상 생성 실패: {}", article.getTitle(), e);
            throw new RuntimeException("영상 생성 중 오류 발생", e);
        }
    }

    /**
     * 기사 내용을 30초 분량 스크립트로 변환
     */
    private String generateScript(Article article) {
        StringBuilder script = new StringBuilder();
        
        // 제목 (5초)
        script.append("오늘의 뉴스입니다. ");
        script.append(article.getTitle()).append(". ");
        
        // 요약 내용 (20초)
        String summary = article.getSummary();
        if (summary != null && !summary.isEmpty()) {
            // 요약을 적절한 길이로 자르기 (약 200자 = 20초)
            if (summary.length() > 200) {
                summary = summary.substring(0, 200) + "...";
            }
            script.append(summary).append(" ");
        }
        
        // 마무리 (5초)
        script.append("자세한 내용은 ").append(article.getSource()).append("에서 확인하세요.");
        
        return script.toString();
    }

    /**
     * 음성 파일과 이미지로 영상 생성
     */
    private File createVideoWithAudioAndImage(File audioFile, Article article) throws Exception {
        // 출력 디렉토리 생성
        Path outputDir = Paths.get(videoConfig.getOutputDirectory());
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // 출력 파일명 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("news_%s_%s.mp4", 
            article.getId(), timestamp);
        File outputFile = outputDir.resolve(fileName).toFile();

        try {
            log.info("영상 생성 시작: {}", outputFile.getName());
            
            // 이미지 파일 준비
            File imageFile = prepareImageFile(article);
            
            // FFmpeg 명령어 구성 (세로형 숏폼)
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(
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
                outputFile.getAbsolutePath()
            );

            log.info("FFmpeg 명령어 실행 중...");
            Process process = processBuilder.start();
            
            // 출력 스트림 읽기 (에러 디버깅용)
            process.getInputStream().transferTo(System.out);
            process.getErrorStream().transferTo(System.err);
            
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 실행 실패: " + exitCode);
            }

            log.info("영상 생성 완료: {} (크기: {}bytes)", outputFile.getName(), outputFile.length());
            return outputFile;

        } catch (Exception e) {
            log.error("영상 생성 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 기사 이미지 파일 준비 (다운로드 또는 기본 이미지)
     */
    private File prepareImageFile(Article article) throws IOException {
        String imageUrl = article.getImageUrl();
        
        // 이미지 URL이 있으면 다운로드 시도
        if (imageUrl != null && imageUrl.startsWith("http")) {
            try {
                return downloadImageFromUrl(imageUrl, article.getId());
            } catch (Exception e) {
                log.warn("이미지 다운로드 실패, 기본 이미지 사용: {}", e.getMessage());
                return createDefaultImageFile(article);
            }
        }
        
        // 기본 이미지 생성
        return createDefaultImageFile(article);
    }

    /**
     * URL에서 이미지 다운로드
     */
    private File downloadImageFromUrl(String imageUrl, Long articleId) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String fileName = String.format("news_image_%s.jpg", articleId);
        File imageFile = tempDir.resolve(fileName).toFile();
        
        log.info("이미지 다운로드 중: {}", imageUrl);
        
        ProcessBuilder processBuilder = new ProcessBuilder(
            "curl", "-L", "-o", imageFile.getAbsolutePath(), imageUrl
        );
        
        Process process = processBuilder.start();
        try {
            int exitCode = process.waitFor();
            if (exitCode == 0 && imageFile.exists() && imageFile.length() > 0) {
                log.info("이미지 다운로드 완료: {} ({}bytes)", imageFile.getName(), imageFile.length());
                return imageFile;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        throw new IOException("이미지 다운로드 실패");
    }

    /**
     * 기본 이미지 파일 생성 (FFmpeg로 단색 배경)
     */
    private File createDefaultImageFile(Article article) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String fileName = String.format("default_bg_%s.jpg", article.getId());
        File imageFile = tempDir.resolve(fileName).toFile();
        
        try {
            log.info("기본 이미지 생성 중...");
            
            // FFmpeg로 파란색 배경 이미지 생성
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "lavfi",
                "-i", "color=c=blue:size=1080x1920:duration=1",
                "-frames:v", "1",
                imageFile.getAbsolutePath()
            );
            
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && imageFile.exists()) {
                log.info("기본 이미지 생성 완료: {}", imageFile.getName());
                return imageFile;
            }
            
        } catch (Exception e) {
            log.warn("FFmpeg 기본 이미지 생성 실패: {}", e.getMessage());
        }
        
        // FFmpeg 실패시 간단한 텍스트 파일 생성 (최후 수단)
        try {
            Files.write(imageFile.toPath(), "뉴스 배경".getBytes());
            return imageFile;
        } catch (Exception e) {
            throw new IOException("기본 이미지 생성 불가", e);
        }
    }

    /**
     * 영상 생성 가능 여부 확인
     */
    public boolean isVideoGenerationEnabled() {
        return videoConfig.isEnabled();
    }
}
