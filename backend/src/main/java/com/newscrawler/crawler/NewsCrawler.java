package com.newscrawler.crawler;

import com.newscrawler.entity.Article;
import com.newscrawler.service.TextCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCrawler {

    private final TextCleanupService textCleanupService;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int TIMEOUT = 30000;

    /**
     * 네이버 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlNaverNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("네이버 뉴스 크롤링 시작");
            
            // 한겨레 RSS 피드 사용 (더 안정적)
            String url = "https://www.hani.co.kr/rss/";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("뉴스");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("한겨레")
                                .category("종합")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("네이버 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("한겨레 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("한겨레 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * YTN 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlDaumNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("연합뉴스 크롤링 시작");
            
            String url = "https://www.yna.co.kr/rss/allnews.xml";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("뉴스");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("연합뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("YTN 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("중앙일보 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("중앙일보 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * MBC 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlZDNetKorea() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("KBS 뉴스 크롤링 시작");
            
            String url = "https://world.kbs.co.kr/rss/rss_news.htm";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("뉴스");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("KBS 뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("KBS 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("KBS 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("KBS 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }



    /**
     * SBS 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlSportsNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("SBS 뉴스 크롤링 시작");
            
            String url = "https://news.sbs.co.kr/news/SectionRssFeed.do?sectionId=01";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("뉴스");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("SBS 뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("SBS 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("SBS 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("SBS 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * RSS 피드에서 이미지 추출
     */
    private String extractImageFromRSS(Element item) {
        try {
            // enclosure 태그에서 이미지 찾기
            Element enclosure = item.selectFirst("enclosure[type*=image]");
            if (enclosure != null) {
                return enclosure.attr("url");
            }
            
            // media:thumbnail 또는 media:content 찾기
            Element mediaThumbnail = item.selectFirst("media|thumbnail, thumbnail");
            if (mediaThumbnail != null) {
                return mediaThumbnail.attr("url");
            }
            
            Element mediaContent = item.selectFirst("media|content[type*=image], content[type*=image]");
            if (mediaContent != null) {
                return mediaContent.attr("url");
            }
            
            // description 안의 img 태그 찾기
            Element description = item.selectFirst("description");
            if (description != null) {
                String descText = description.text();
                Document descDoc = Jsoup.parse(descText);
                Element img = descDoc.selectFirst("img");
                if (img != null) {
                    return img.attr("src");
                }
            }
            
        } catch (Exception e) {
            log.debug("RSS에서 이미지 추출 실패: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 기사 페이지에서 이미지 추출
     */
    private String extractImageFromArticle(String articleUrl) {
        try {
            log.debug("기사 페이지에서 이미지 추출 시도: {}", articleUrl);
            
            Document doc = Jsoup.connect(articleUrl)
                    .userAgent(USER_AGENT)
                    .timeout(10000) // 짧은 타임아웃
                    .get();
            
            // Open Graph 이미지 우선
            Element ogImage = doc.selectFirst("meta[property=og:image]");
            if (ogImage != null && !ogImage.attr("content").isEmpty()) {
                return ogImage.attr("content");
            }
            
            // Twitter Card 이미지
            Element twitterImage = doc.selectFirst("meta[name=twitter:image]");
            if (twitterImage != null && !twitterImage.attr("content").isEmpty()) {
                return twitterImage.attr("content");
            }
            
            // 기사 본문의 첫 번째 이미지
            Element firstImg = doc.selectFirst("article img, .article img, .news-content img, .content img, main img");
            if (firstImg != null && !firstImg.attr("src").isEmpty()) {
                String src = firstImg.attr("src");
                // 상대경로면 절대경로로 변환
                if (src.startsWith("/")) {
                    String baseUrl = articleUrl.substring(0, articleUrl.indexOf("/", 8));
                    src = baseUrl + src;
                }
                return src;
            }
            
            // 아무 이미지나 찾기 (크기 필터링)
            Elements allImages = doc.select("img[src]");
            for (Element img : allImages) {
                String src = img.attr("src");
                if (src.contains("logo") || src.contains("icon") || src.contains("btn")) {
                    continue; // 로고나 아이콘 제외
                }
                if (src.startsWith("/")) {
                    String baseUrl = articleUrl.substring(0, articleUrl.indexOf("/", 8));
                    src = baseUrl + src;
                }
                return src;
            }
            
        } catch (Exception e) {
            log.debug("기사 페이지 이미지 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 연예뉴스 크롤링 (연합뉴스 연예)
     */
    public List<Article> crawlEntertainmentNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("연예뉴스 크롤링 시작");
            
            String url = "https://www.yna.co.kr/rss/entertainment.xml";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("연예");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("연합뉴스")
                                .category("연예")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("연예뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("연예뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("연예뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * 경제뉴스 크롤링 (매일경제)
     */
    public List<Article> crawlEconomyNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("경제뉴스 크롤링 시작");
            
            String url = "https://www.mk.co.kr/rss/30000001/";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("경제");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("매일경제")
                                .category("경제")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("경제뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("경제뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("경제뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * 스포츠뉴스 크롤링 (스포츠조선)
     */
    public List<Article> crawlSportsNewsExtra() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("스포츠뉴스 크롤링 시작");
            
            String url = "http://sports.chosun.com/rss/sports.xml";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .parser(org.jsoup.parser.Parser.xmlParser())
                    .get();

            Elements items = doc.select("item");
            
            for (Element item : items) {
                try {
                    String title = item.selectFirst("title").text().trim();
                    String link = item.selectFirst("link").text().trim();
                    String description = item.selectFirst("description") != null ? 
                            item.selectFirst("description").text().trim() : "";
                    
                    if (!title.isEmpty()) {
                        // 이미지 추출 시도
                        String imageUrl = extractImageFromRSS(item);
                        if (imageUrl == null) {
                            imageUrl = extractImageFromArticle(link);
                        }
                        if (imageUrl == null) {
                            imageUrl = getDefaultImageUrl("스포츠");
                        }
                        
                        // 텍스트 정리 적용
                        String cleanTitle = textCleanupService.cleanTitle(title);
                        String cleanDescription = textCleanupService.cleanSummary(description);
                        if (cleanDescription.length() > 300) {
                            cleanDescription = cleanDescription.substring(0, 300) + "...";
                        }
                        
                        Article article = Article.builder()
                                .title(cleanTitle)
                                .summary(cleanDescription)
                                .source("스포츠조선")
                                .category("스포츠")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("스포츠뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("스포츠뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("스포츠뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * 기본 이미지 URL 반환
     */
    private String getDefaultImageUrl(String category) {
        return switch (category.toLowerCase()) {
            case "it", "디지털" -> "https://via.placeholder.com/300x200/0066CC/FFFFFF?text=IT+뉴스";
            case "글로벌" -> "https://via.placeholder.com/300x200/009900/FFFFFF?text=Global+News";
            case "스포츠" -> "https://via.placeholder.com/300x200/FF6600/FFFFFF?text=스포츠+뉴스";
            case "연예" -> "https://via.placeholder.com/300x200/FF1493/FFFFFF?text=연예+뉴스";
            case "경제" -> "https://via.placeholder.com/300x200/228B22/FFFFFF?text=경제+뉴스";
            case "뉴스", "종합" -> "https://via.placeholder.com/300x200/CC3333/FFFFFF?text=한국+뉴스";
            default -> "https://via.placeholder.com/300x200/666666/FFFFFF?text=뉴스";
        };
    }
}
