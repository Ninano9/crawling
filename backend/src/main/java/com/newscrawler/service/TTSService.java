package com.newscrawler.service;

import com.newscrawler.config.TTSConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TTSService {

    private final TTSConfig ttsConfig;

    /**
     * 텍스트를 음성 파일로 변환 (무료 TTS 사용)
     */
    public File generateAudio(String text) {
        try {
            log.info("===== 무료 TTS 음성 생성 시작: {} 글자 =====", text.length());
            
            String provider = ttsConfig.getProvider().toLowerCase();
            
            switch (provider) {
                case "edge-tts":
                    return generateAudioWithEdgeTTS(text);
                case "espeak":
                    return generateAudioWithESpeak(text);
                default:
                    log.warn("알 수 없는 TTS 제공자: {}, Edge-TTS 사용", provider);
                    return generateAudioWithEdgeTTS(text);
            }
            
        } catch (Exception e) {
            log.error("TTS 음성 생성 실패", e);
            return createSilentAudioFile();
        }
    }

    /**
     * Edge-TTS로 음성 생성 (Microsoft의 무료 TTS)
     */
    private File generateAudioWithEdgeTTS(String text) throws Exception {
        log.info("Edge-TTS로 음성 생성 중...");
        
        File audioFile = createTempAudioFile("mp3");
        
        // Edge-TTS Python 스크립트 실행
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
            "python", "-c",
            String.format("""
                import asyncio
                import edge_tts
                
                async def main():
                    voice = "%s"
                    text = '''%s'''
                    rate = "%s"
                    pitch = "%s"
                    
                    communicate = edge_tts.Communicate(text, voice, rate=rate, pitch=pitch)
                    await communicate.save("%s")
                
                if __name__ == "__main__":
                    asyncio.run(main())
                """,
                ttsConfig.getEdge().getVoice(),
                text.replace("'", "\\'"),
                ttsConfig.getEdge().getRate(),
                ttsConfig.getEdge().getPitch(),
                audioFile.getAbsolutePath().replace("\\", "/")
            )
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Edge-TTS 실행 시간 초과");
        }
        
        if (process.exitValue() != 0) {
            log.warn("Edge-TTS 실패, eSpeak으로 대체");
            return generateAudioWithESpeak(text);
        }
        
        log.info("Edge-TTS 음성 생성 완료: {}", audioFile.getName());
        return audioFile;
    }

    /**
     * eSpeak으로 음성 생성 (오픈소스 TTS)
     */
    private File generateAudioWithESpeak(String text) throws Exception {
        log.info("eSpeak으로 음성 생성 중...");
        
        File audioFile = createTempAudioFile("wav");
        
        // eSpeak 명령어 실행
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(
            "espeak",
            "-v", ttsConfig.getEspeak().getVoice(),
            "-s", String.valueOf(ttsConfig.getEspeak().getSpeed()),
            "-p", String.valueOf(ttsConfig.getEspeak().getPitch()),
            "-w", audioFile.getAbsolutePath(),
            text
        );
        
        Process process = processBuilder.start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("eSpeak 실행 시간 초과");
        }
        
        if (process.exitValue() != 0) {
            throw new RuntimeException("eSpeak 실행 실패");
        }
        
        log.info("eSpeak 음성 생성 완료: {}", audioFile.getName());
        return audioFile;
    }

    /**
     * 임시 음성 파일 생성
     */
    private File createTempAudioFile(String extension) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("tts_audio_%s.%s", timestamp, extension);
        
        return tempDir.resolve(fileName).toFile();
    }

    /**
     * 무음 오디오 파일 생성 (TTS 실패시 대체용)
     */
    private File createSilentAudioFile() {
        try {
            log.warn("TTS 서비스 사용 불가, 무음 파일 생성");
            
            File silentFile = createTempAudioFile("mp3");
            
            // FFmpeg로 30초 무음 파일 생성
            ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-y",
                "-f", "lavfi",
                "-i", "anullsrc=channel_layout=stereo:sample_rate=44100",
                "-t", "30",
                "-c:a", "mp3",
                silentFile.getAbsolutePath()
            );

            Process process = processBuilder.start();
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("무음 파일 생성 시간 초과");
            }

            if (process.exitValue() == 0) {
                return silentFile;
            } else {
                throw new RuntimeException("무음 파일 생성 실패");
            }

        } catch (Exception e) {
            log.error("무음 파일 생성 실패", e);
            throw new RuntimeException("음성 파일 생성 불가", e);
        }
    }
}
