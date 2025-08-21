package com.newscrawler.crawler;

import com.newscrawler.entity.Article;
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
@Slf4j
public class NewsCrawler {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int TIMEOUT = 30000;

    /**
     * 네이버 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlNaverNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("네이버 뉴스 크롤링 시작");
            
            // 네이버 뉴스 RSS 피드 사용
            String url = "https://rss.news.naver.com/services/rss/AllHeadLines.xml";
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
                        Article article = Article.builder()
                                .title(title)
                                .summary(description.length() > 300 ? description.substring(0, 300) + "..." : description)
                                .source("네이버 뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("뉴스"))
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("네이버 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("네이버 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("네이버 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * YTN 뉴스 크롤링 (RSS 사용)
     */
    public List<Article> crawlDaumNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("YTN 뉴스 크롤링 시작");
            
            String url = "https://www.ytn.co.kr/rss/top.xml";
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
                        Article article = Article.builder()
                                .title(title)
                                .summary(description.length() > 300 ? description.substring(0, 300) + "..." : description)
                                .source("YTN 뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("뉴스"))
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("YTN 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("YTN 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("YTN 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * ZDNet 코리아 크롤링 (IT 전문)
     */
    public List<Article> crawlZDNetKorea() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("ZDNet 코리아 크롤링 시작");
            
            String url = "https://zdnet.co.kr/news/";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements newsItems = doc.select(".news-list .news-item");
            
            for (Element item : newsItems) {
                try {
                    Element titleElement = item.selectFirst(".news-title a");
                    Element summaryElement = item.selectFirst(".news-summary");
                    Element imageElement = item.selectFirst(".news-thumb img");
                    
                    if (titleElement != null) {
                        String title = titleElement.text().trim();
                        String link = titleElement.attr("abs:href");
                        String summary = summaryElement != null ? summaryElement.text().trim() : "";
                        String imageUrl = imageElement != null ? imageElement.attr("abs:src") : getDefaultImageUrl("IT");
                        
                        Article article = Article.builder()
                                .title(title)
                                .summary(summary.length() > 300 ? summary.substring(0, 300) + "..." : summary)
                                .source("ZDNet 코리아")
                                .category("IT")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("ZDNet 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("ZDNet 코리아 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("ZDNet 코리아 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * BBC 뉴스 크롤링 (글로벌)
     */
    public List<Article> crawlBBCNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("BBC 뉴스 크롤링 시작");
            
            String url = "https://www.bbc.com/news/technology";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements newsItems = doc.select("[data-testid=\"card-headline\"]");
            
            for (Element item : newsItems) {
                try {
                    Element titleElement = item.selectFirst("h2");
                    
                    if (titleElement != null) {
                        String title = titleElement.text().trim();
                        String link = "https://www.bbc.com" + item.attr("href");
                        
                        Article article = Article.builder()
                                .title(title)
                                .summary("BBC Technology News")
                                .source("BBC News")
                                .category("글로벌 기술")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("글로벌"))
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("BBC 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("BBC 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("BBC 뉴스 크롤링 실패: {}", e.getMessage());
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
                        Article article = Article.builder()
                                .title(title)
                                .summary(description.length() > 300 ? description.substring(0, 300) + "..." : description)
                                .source("SBS 뉴스")
                                .category("종합")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("뉴스"))
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
     * 기본 이미지 URL 반환
     */
    private String getDefaultImageUrl(String category) {
        return switch (category.toLowerCase()) {
            case "it", "디지털" -> "https://via.placeholder.com/300x200/0066CC/FFFFFF?text=IT+뉴스";
            case "글로벌" -> "https://via.placeholder.com/300x200/009900/FFFFFF?text=Global+News";
            case "스포츠" -> "https://via.placeholder.com/300x200/FF6600/FFFFFF?text=스포츠+뉴스";
            case "뉴스", "종합" -> "https://via.placeholder.com/300x200/CC3333/FFFFFF?text=한국+뉴스";
            default -> "https://via.placeholder.com/300x200/666666/FFFFFF?text=뉴스";
        };
    }
}
