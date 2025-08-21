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
     * 네이버 뉴스 크롤링
     */
    public List<Article> crawlNaverNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("네이버 뉴스 크롤링 시작");
            
            // IT/과학 섹션
            String url = "https://news.naver.com/main/main.naver?mode=LSD&mid=shm&sid1=105";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements newsItems = doc.select(".cluster_body .cluster_text");
            
            for (Element item : newsItems) {
                try {
                    Element titleElement = item.selectFirst("a.cluster_text_headline");
                    Element summaryElement = item.selectFirst(".cluster_text_lede");
                    
                    if (titleElement != null) {
                        String title = titleElement.text().trim();
                        String link = "https://news.naver.com" + titleElement.attr("href");
                        String summary = summaryElement != null ? summaryElement.text().trim() : "";
                        
                        Article article = Article.builder()
                                .title(title)
                                .summary(summary.length() > 300 ? summary.substring(0, 300) + "..." : summary)
                                .source("네이버 뉴스")
                                .category("IT/과학")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("IT"))
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
     * 다음 뉴스 크롤링
     */
    public List<Article> crawlDaumNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("다음 뉴스 크롤링 시작");
            
            String url = "https://news.daum.net/breakingnews/digital";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements newsItems = doc.select(".list_news2 .item_news");
            
            for (Element item : newsItems) {
                try {
                    Element titleElement = item.selectFirst(".link_txt");
                    Element summaryElement = item.selectFirst(".desc");
                    Element imageElement = item.selectFirst(".thumb_g img");
                    
                    if (titleElement != null) {
                        String title = titleElement.text().trim();
                        String link = titleElement.attr("href");
                        String summary = summaryElement != null ? summaryElement.text().trim() : "";
                        String imageUrl = imageElement != null ? imageElement.attr("src") : getDefaultImageUrl("디지털");
                        
                        Article article = Article.builder()
                                .title(title)
                                .summary(summary.length() > 300 ? summary.substring(0, 300) + "..." : summary)
                                .source("다음 뉴스")
                                .category("디지털")
                                .link(link)
                                .imageUrl(imageUrl)
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("다음 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("다음 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("다음 뉴스 크롤링 실패: {}", e.getMessage());
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
     * 스포츠 뉴스 크롤링 (네이버 스포츠)
     */
    public List<Article> crawlSportsNews() {
        List<Article> articles = new ArrayList<>();
        try {
            log.info("스포츠 뉴스 크롤링 시작");
            
            String url = "https://sports.news.naver.com/";
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT)
                    .get();

            Elements newsItems = doc.select(".home_news .news_list li");
            
            for (Element item : newsItems) {
                try {
                    Element titleElement = item.selectFirst("a");
                    
                    if (titleElement != null) {
                        String title = titleElement.text().trim();
                        String link = titleElement.attr("abs:href");
                        
                        Article article = Article.builder()
                                .title(title)
                                .summary("네이버 스포츠 뉴스")
                                .source("네이버 스포츠")
                                .category("스포츠")
                                .link(link)
                                .imageUrl(getDefaultImageUrl("스포츠"))
                                .publishedAt(LocalDateTime.now())
                                .build();
                        
                        articles.add(article);
                        
                        if (articles.size() >= 10) break;
                    }
                } catch (Exception e) {
                    log.warn("스포츠 뉴스 개별 아이템 파싱 실패: {}", e.getMessage());
                }
            }
            
            log.info("스포츠 뉴스 크롤링 완료: {}개 기사", articles.size());
            
        } catch (Exception e) {
            log.error("스포츠 뉴스 크롤링 실패: {}", e.getMessage());
        }
        
        return articles;
    }

    /**
     * 기본 이미지 URL 반환
     */
    private String getDefaultImageUrl(String category) {
        return switch (category.toLowerCase()) {
            case "it", "디지털" -> "https://via.placeholder.com/300x200/0066CC/FFFFFF?text=IT+News";
            case "글로벌" -> "https://via.placeholder.com/300x200/009900/FFFFFF?text=Global+News";
            case "스포츠" -> "https://via.placeholder.com/300x200/FF6600/FFFFFF?text=Sports+News";
            default -> "https://via.placeholder.com/300x200/666666/FFFFFF?text=News";
        };
    }
}
