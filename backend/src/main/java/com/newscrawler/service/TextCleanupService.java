package com.newscrawler.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
public class TextCleanupService {

    // HTML 태그 제거 패턴
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    
    // HTML 엔티티 패턴들
    private static final Pattern HTML_ENTITY_PATTERN = Pattern.compile("&[a-zA-Z0-9#]+;");
    
    // 연속된 공백 제거 패턴
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");
    
    // 특수문자 정리 패턴
    private static final Pattern SPECIAL_CHARS_PATTERN = Pattern.compile("[\\r\\n\\t]+");

    /**
     * HTML 코드가 포함된 텍스트를 깔끔하게 정리
     */
    public String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        try {
            // 1. HTML 태그 제거
            String cleaned = HTML_TAG_PATTERN.matcher(text).replaceAll("");
            
            // 2. HTML 엔티티 디코딩
            cleaned = decodeHtmlEntities(cleaned);
            
            // 3. 특수문자 정리 (줄바꿈, 탭 등)
            cleaned = SPECIAL_CHARS_PATTERN.matcher(cleaned).replaceAll(" ");
            
            // 4. 연속된 공백을 하나로 통일
            cleaned = MULTIPLE_SPACES_PATTERN.matcher(cleaned).replaceAll(" ");
            
            // 5. 앞뒤 공백 제거
            cleaned = cleaned.trim();
            
            log.debug("텍스트 정리: {} → {}", text.substring(0, Math.min(50, text.length())), 
                cleaned.substring(0, Math.min(50, cleaned.length())));
            
            return cleaned;
            
        } catch (Exception e) {
            log.error("텍스트 정리 중 오류 발생", e);
            return text; // 실패시 원본 반환
        }
    }

    /**
     * HTML 엔티티를 실제 문자로 변환
     */
    private String decodeHtmlEntities(String text) {
        return text
            // 자주 사용되는 HTML 엔티티들
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            
            // 공백 관련
            .replace("&nbsp;", " ")
            .replace("&#160;", " ")
            
            // 특수문자
            .replace("&copy;", "©")
            .replace("&reg;", "®")
            .replace("&trade;", "™")
            
            // 화살표
            .replace("&rarr;", "→")
            .replace("&larr;", "←")
            
            // 따옴표
            .replace("&ldquo;", "\"")
            .replace("&rdquo;", "\"")
            .replace("&lsquo;", "'")
            .replace("&rsquo;", "'")
            
            // 대시
            .replace("&ndash;", "–")
            .replace("&mdash;", "—")
            
            // 나머지 숫자 엔티티 (&#숫자; 형태)
            .replaceAll("&#(\\d+);", this::convertNumericEntity);
    }

    /**
     * 숫자 HTML 엔티티를 문자로 변환
     */
    private String convertNumericEntity(String match) {
        try {
            String numberStr = match.replaceAll("[&#;]", "");
            int charCode = Integer.parseInt(numberStr);
            
            // 안전한 범위의 문자만 변환
            if (charCode >= 32 && charCode <= 126) {
                return String.valueOf((char) charCode);
            } else if (charCode >= 0x80 && charCode <= 0xFFFF) {
                return String.valueOf((char) charCode);
            }
        } catch (NumberFormatException e) {
            log.debug("숫자 엔티티 변환 실패: {}", match);
        }
        
        return " "; // 변환 실패시 공백으로 대체
    }

    /**
     * 기사 제목 정리 (더 엄격한 정리)
     */
    public String cleanTitle(String title) {
        String cleaned = cleanText(title);
        
        // 제목에서 불필요한 패턴 제거
        cleaned = cleaned
            .replaceAll("\\[.*?\\]", "") // [대괄호] 제거
            .replaceAll("\\(.*?\\)", "") // (소괄호) 제거 (선택적)
            .replaceAll("【.*?】", "")   // 【특수괄호】 제거
            .replaceAll("「.*?」", "")   // 「인용부호」 제거
            .replaceAll("\\s*-\\s*$", "") // 끝의 대시 제거
            .trim();
        
        return cleaned;
    }

    /**
     * 기사 요약 정리
     */
    public String cleanSummary(String summary) {
        String cleaned = cleanText(summary);
        
        // 요약에서 불필요한 패턴 제거
        cleaned = cleaned
            .replaceAll("^[\\s\\-•]*", "") // 시작의 불릿포인트나 대시 제거
            .replaceAll("[\\s\\-•]*$", "") // 끝의 불릿포인트나 대시 제거
            .trim();
        
        return cleaned;
    }

    /**
     * TTS용 텍스트 정리 (발음하기 좋게)
     */
    public String cleanForTTS(String text) {
        String cleaned = cleanText(text);
        
        // TTS를 위한 추가 정리
        cleaned = cleaned
            // 영어 약어 처리
            .replaceAll("\\bCEO\\b", "씨이오")
            .replaceAll("\\bCTO\\b", "씨티오")
            .replaceAll("\\bIT\\b", "아이티")
            .replaceAll("\\bAI\\b", "에이아이")
            .replaceAll("\\bAPI\\b", "에이피아이")
            
            // 숫자와 단위
            .replaceAll("(\\d+)km", "$1킬로미터")
            .replaceAll("(\\d+)kg", "$1킬로그램")
            .replaceAll("(\\d+)%", "$1퍼센트")
            
            // 특수 기호
            .replace("&", "그리고")
            .replace("@", "at")
            .replace("#", "샵")
            
            // 연속된 마침표나 물음표 정리
            .replaceAll("\\.{2,}", ".")
            .replaceAll("\\?{2,}", "?")
            .replaceAll("!{2,}", "!")
            
            .trim();
        
        return cleaned;
    }
}
