package src;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class News {
    public static void main(String[] args) throws IOException {

        int page = 5;

        for(int j=1; j < page; j++) {
            String url = "https://news.naver.com/main/list.naver?mode=LS2D&mid=shm&sid2=265&sid1=100&date=20210811&page=" + j;
            Document doc = Jsoup.connect(url).get();
            Elements elements = doc.getElementsByAttributeValue("class", "list_body newsflash_body");

            Element element = elements.get(0);
            Elements photoElements = element.getElementsByAttributeValue("class", "photo");

            for(int i=0; i<photoElements.size(); i++) {
                Element articleElement = photoElements.get(i);
                Elements aElements = articleElement.select("a");
                Element aElement = aElements.get(0);

                String articleUrl = aElement.attr("href");		// 기사링크

                Element imgElement = aElement.select("img").get(0);
                String imgUrl = imgElement.attr("src");			// 사진링크
                String title = imgElement.attr("alt");			// 기사제목

                Document subDoc = Jsoup.connect(articleUrl).get();
                Element contentElement = subDoc.getElementById("articleBodyContents");
                String content = contentElement.text();			// 기사내용

                System.out.println(title);
                System.out.println(content);
                System.out.println();
            }
            System.out.println(j + "page 크롤링 종료");
        }

    }
}