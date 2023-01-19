package searchengine.services.pageconvertor;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.SitesList;
import searchengine.dto.PageDto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class PagesIndex extends RecursiveTask<List<PageDto>> {
    private final String url;
    private final List<String> urlList;
    private final List<PageDto> pageDtoList;

    private final SitesList sitesList;

    public PagesIndex(String url, List<String> urlList, List<PageDto> pageDtoList, SitesList sitesList) {
        this.url = url;
        this.urlList = urlList;
        this.pageDtoList = pageDtoList;
        this.sitesList = sitesList;
    }

    @Override
    protected List<PageDto> compute() {
        try {
            Thread.sleep(150);
            Document doc = null;
            try {
                Thread.sleep(150);
                doc = Jsoup.connect(url)
                        .userAgent(sitesList.getUserAgent())
                        .referrer(sitesList.getReferrer())
                        .get();
            } catch (Exception exception) {
                log.debug("Error connecting: " + url);
            }
            String html = doc.outerHtml();
            Connection.Response response = doc.connection().response();
            int status = response.statusCode();
            PageDto pageDto = new PageDto(url, html, status);
            pageDtoList.add(pageDto);
            Elements elements = doc.select("body").select("a");
            List<PagesIndex> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");
                if (link.startsWith(el.baseUri()) && !link.equals(el.baseUri()) && !link.contains("#") && !link.contains(".pdf") && !link.contains(".jpg") && !link.contains(".JPG") && !link.contains(".png") && !urlList.contains(link)) {

                    urlList.add(link);
                    PagesIndex task = new PagesIndex(link,urlList, pageDtoList, sitesList);
                    task.fork();
                    taskList.add(task);
                }
            }
            taskList.forEach(ForkJoinTask::join);
        } catch (Exception e) {
            log.debug("Ошибка парсинга - " + url);
            PageDto pageDto = new PageDto(url, "", 500);
            pageDtoList.add(pageDto);
        }
        return pageDtoList;
    }
}
