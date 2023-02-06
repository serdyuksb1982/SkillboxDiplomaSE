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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;


@Slf4j
public class PageIndexer extends RecursiveTask<List<PageDto>> {
    private final String url;
    private final List<String> urlList;
    private final List<PageDto> pageDtoList;
    private final SitesList config;

    public PageIndexer(String url,
                       List<PageDto> pageDtoList,
                       List<String> urlList,
                       SitesList config) {
        this.url = url;
        this.pageDtoList = pageDtoList;
        this.urlList = urlList;
        this.config = config;
    }

    @Override
    protected List<PageDto> compute() {
        try {
            Thread.sleep(100);
            Document doc = null;
            try {
                Thread.sleep(100);
                doc = Jsoup.connect(url)
                        .userAgent(config.getUserAgent())
                        .referrer(config.getReferrer())
                        .get();
            } catch (Exception e) {
                e.getMessage();
            }
            assert doc != null;
            String html = doc.outerHtml();
            Connection.Response response = doc.connection().response();
            int status = response.statusCode();
            PageDto pageDto = new PageDto(url, html, status);
            pageDtoList.add(pageDto);
            Elements elements = doc.select("body")
                    .select("a");
            List<PageIndexer> taskList = new ArrayList<>();
            for (Element el : elements) {
                String link = el.attr("abs:href");
                if (isSiteElementsType(link)) {
                    if (link.startsWith(el.baseUri())
                            && !link.equals(el.baseUri())
                            && !link.contains("#")
                            && !urlList.contains(link)) {
                        urlList.add(link);
                        PageIndexer task = new PageIndexer(link, pageDtoList, urlList, config);
                        task.fork();
                        taskList.add(task);
                    }
                }
            }
            taskList.forEach(ForkJoinTask::join);
        } catch (Exception e) {
            log.debug("Error parsing from ".concat(url));
            PageDto pageDto = new PageDto(url, "", 500);
            pageDtoList.add(pageDto);
        }
        return pageDtoList;
    }

    private boolean isSiteElementsType(String pathPage) {
        List<String> WRONG_TYPES = Arrays.asList(
                "JPG", "gif", "gz", "jar", "jpeg", "jpg", "pdf", "png", "ppt", "pptx", "svg", "svg", "tar", "zip");
        return !WRONG_TYPES.contains(pathPage.substring(pathPage.lastIndexOf(".") + 1));
    }


}