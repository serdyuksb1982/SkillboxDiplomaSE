package searchengine.services;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import searchengine.config.SitesList;

import searchengine.dto.statistics.model.PageModel;
import searchengine.dto.statistics.model.SiteModel;
import searchengine.dto.statistics.emum.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Getter
public class SitesParseService extends RecursiveTask<Integer> {
    private static Set<String> websites = new CopyOnWriteArraySet<>();

    private AtomicInteger pageId;
    private String mainPage = new String("");
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    private SitesList config;

    private Integer pageCount;
    private final List<SitesParseService> pageChildren;
    private String startPage;
    private final SiteModel site;

    public SitesParseService(String startPage,
                             SiteModel site,
                             String mainPage,
                             PageRepository pageRepository,
                             SiteRepository siteRepository,
                             SitesList config,
                             AtomicInteger pageId) {
        this.startPage = startPage;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.config = config;
        this.pageId = pageId;
        pageChildren = new ArrayList<>();
        pageCount = 0;
        websites.add(startPage);
        if (this.mainPage.equals("")) {
            this.mainPage = mainPage;
        }
    }

    public SitesParseService(String startPage,
                             SiteModel site,
                             SitesList config,
                             SiteRepository siteRepository,
                             PageRepository pageRepository) {
        this.startPage = startPage;
        this.site = site;
        pageChildren = new ArrayList<>();
        this.pageId = new AtomicInteger(0);
        websites.add(startPage);
        websites.add(startPage.concat("/") );
        pageCount = 0;

        if (mainPage.equals("")) mainPage = startPage;

        if (this.pageRepository == null) this.pageRepository = pageRepository;

        if (this.siteRepository == null) this.siteRepository = siteRepository;

        this.config = config;
    }

    @Override
    protected Integer compute() {
        if (isSiteElementsType(startPage)) {
            try {
                if (!startPage.endsWith("/")) {
                    startPage += "/";
                }
                synchronized (pageId) {
                    pageId.getAndIncrement();

                    Connection.Response response = Jsoup.connect(startPage)
                            .ignoreHttpErrors(true)
                            .userAgent(config.getUserAgent())
                            .referrer(config.getReferrer())
                            .execute();

                    Document document = response.parse();

                    Thread.sleep(1000);

                    addNewSitePage(response, document);

                    Elements elements = document.select("a");
                    for (Element element : elements) {

                        String attr = element.attr("href");
                        if (!attr.contains("http")) {
                            if (!attr.startsWith("/") && attr.length() > 1) {
                                attr = "/".concat(attr);
                            }
                            attr = mainPage.concat(attr);
                        }
                        if (attr.contains(mainPage) && !websites.contains(attr) && !attr.contains("#")) {
                            newChild(attr);
                        }
                    }
                }
            } catch (IOException | InterruptedException | NullPointerException exception) {
                site.setLastError("Error: ".concat(exception.getMessage()));
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
            }
            for (SitesParseService it : pageChildren) {
                pageCount += it.join();
            }
        }
        return pageCount;
    }

    private void newChild(String attr) {
        websites.add(attr);
        SitesParseService newChild = new SitesParseService(attr, site, mainPage, pageRepository, siteRepository, config, pageId);
        newChild.fork();
        pageChildren.add(newChild);
    }

    private boolean isSiteElementsType(String pathPage) {
        List<String> WRONG_TYPES = Arrays.asList(
                "jpeg", "jpg", "pdf",
                "png", "gif", "zip",
                "tar", "jar", "gz",
                "svg", "ppt", "pptx");
        if (!WRONG_TYPES.contains(pathPage.substring(pathPage.lastIndexOf(".") + 1))) {
            return true;
        }
        else return false;
    }

    private PageModel addNewSitePage(Connection.Response response, Document parse) {
        PageModel page = pageRepository.findByPath(startPage);
        if (page == null) page = new PageModel();

        page.setCode(response.statusCode());
        page.setPath(startPage);
        page.setContent(parse.html());
        page.setSite(site);

        pageRepository.save(page);
        return page;
    }
}