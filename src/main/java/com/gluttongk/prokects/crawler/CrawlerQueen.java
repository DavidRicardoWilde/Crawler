package com.gluttongk.prokects.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CrawlerQueen {
    static final String sina = "https://sina.cn";
    static final String medium = "https://medium.com/";
    static final String github = "https://github.com/";
    static final String qq = "https://www.qq.com/";

    /**
     * Demo
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        //待处理的链接池
        List<String> linkPool = new ArrayList<>();
        //已经处理的链接池
        Set<String> processedLinks = new HashSet<>();

        linkPool.add(sina);

        while (true) {
            if (linkPool.isEmpty()) {
                break;
            }

            String link = linkPool.remove(linkPool.size() - 1);

            if (processedLinks.contains(link)) {
                continue;
            }

            if (isInterestingLink(link)) {
                Document doc = httpGetAndParseHtml(link);

                doc.select("a").stream().map(articleTag->articleTag.attr("href")).forEach(linkPool::add);

                storeNewsPageIntoDatabase(doc);

                processedLinks.add(link);

                System.out.println("current link is " + link);
            }


        }
    }

    private static void storeNewsPageIntoDatabase(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                String title = articleTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36 Edg/81.0.416.72");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);

            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isNewsPage(link) || isTarget(link) && isNotAuthPage(link));
    }

    private static boolean isTarget(String link) {
        return sina.equals(link);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotAuthPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
