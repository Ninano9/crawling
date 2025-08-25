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
            // FFmpeg를 사용한 영상 생성
            // 이미지 + 음성을 결합하여 MP4 생성
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            String imageUrl = article.getImageUrl();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = getDefaultImagePath();
            }

            // FFmpeg 명령어 구성
            processBuilder.command(
                "ffmpeg", "-y",
                "-loop", "1",
                "-i", downloadImageIfNeeded(imageUrl),
                "-i", audioFile.getAbsolutePath(),
                "-c:v", "libx264",
                "-tune", "stillimage",
                "-c:a", "aac",
                "-b:a", "192k",
                "-pix_fmt", "yuv420p",
                "-vf", String.format("scale=%s", videoConfig.getResolution().replace("x", ":")),
                "-shortest",
                "-fflags", "+shortest",
                "-max_interleave_delta", "100M",
                outputFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg 실행 실패: " + exitCode);
            }

            return outputFile;

        } catch (Exception e) {
            log.error("영상 생성 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 이미지 URL에서 로컬 파일로 다운로드 (필요시)
     */
    private String downloadImageIfNeeded(String imageUrl) throws IOException {
        if (imageUrl.startsWith("http")) {
            // TODO: 이미지 다운로드 로직 구현
            // 현재는 기본 이미지 사용
            return getDefaultImagePath();
        }
        return imageUrl;
    }

    /**
     * 기본 이미지 경로 반환
     */
    private String getDefaultImagePath() {
        // 기본 뉴스 이미지 (프로젝트 리소스에 포함)
        return "src/main/resources/static/default-news-bg.jpg";
    }

    /**
     * 영상 생성 가능 여부 확인
     */
    public boolean isVideoGenerationEnabled() {
        return videoConfig.isEnabled();
    }
}
