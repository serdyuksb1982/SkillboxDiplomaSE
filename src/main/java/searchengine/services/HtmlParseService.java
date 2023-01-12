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

import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.enums.Status;
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
public class HtmlParseService extends RecursiveTask<Integer> {
    private static Set<String> websites = new CopyOnWriteArraySet<>();

    private AtomicInteger pageId;
    private String mainPage = "";
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    private final SitesList sitesListConfig;

    private Integer pageCount;
    private final List<HtmlParseService> pageChildren;
    private String startPage;
    private final Site site;

    public HtmlParseService(String startPage,
                            Site site,
                            String mainPage,
                            PageRepository pageRepository,
                            SiteRepository siteRepository,
                            SitesList config,
                            AtomicInteger pageId) {
        this.startPage = startPage;
        this.site = site;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.sitesListConfig = config;
        this.pageId = pageId;
        pageChildren = new ArrayList<>();
        pageCount = 0;
        websites.add(startPage);
        if (this.mainPage.equals("")) {
            this.mainPage = mainPage;
        }
    }

    public HtmlParseService(String startPage,
                            Site site,
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

        this.sitesListConfig = config;
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
                            .userAgent(sitesListConfig.getUserAgent())
                            .referrer(sitesListConfig.getReferrer())
                            .execute();

                    Document document = response.parse();

                    Thread.sleep(500);

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
            for (HtmlParseService it : pageChildren) {
                pageCount += it.join();
            }
        }
        return pageCount;
    }

    private void newChild(String attr) {
        websites.add(attr);
        HtmlParseService newChild = new HtmlParseService(attr, site, mainPage, pageRepository, siteRepository, sitesListConfig, pageId);
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

    private Page addNewSitePage(Connection.Response response, Document parse) {
        Page page = pageRepository.findByPath(startPage);
        if (page == null) page = new Page();

        page.setCode(response.statusCode());
        page.setPath(startPage);
        page.setContent(parse.html());
        page.setSite(site);

        pageRepository.save(page);
        return page;
    }

    public void addPage() throws IOException {
        Connection.Response response = Jsoup.connect(startPage)
                .userAgent(sitesListConfig.getUserAgent())
                .referrer(sitesListConfig.getReferrer())
                .ignoreHttpErrors(true)
                .execute();

        addPage(response, response.parse());
    }

    private void addPage(Connection.Response response, Document document) {
        Page page = pageRepository.findByPath(startPage);
        if (page == null) {
            page = new Page();
        }
        page.setCode(response.statusCode());
        page.setPath(startPage);
        page.setContent(document.html());
        page.setSite(site);

        pageRepository.save(page);
    }

}