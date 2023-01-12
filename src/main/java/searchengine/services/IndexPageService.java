package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteConfiguration;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.enums.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class IndexPageService {

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final SitesList config;

    public IndexPageService(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesListConfig) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.config = sitesListConfig;
    }

    public boolean indexPage(String url) {

        List<Site> siteList = siteRepository.findAll();

        List<SiteConfiguration> sites = config.getSites();

        if (siteList.size() == 0) {
            List<String> urls = new ArrayList<>();
            for (SiteConfiguration site : sites) {
                urls.add(site.getUrl());
            }
            List<String> namesUrls = new ArrayList<>();
            for (SiteConfiguration site : sites) {
                namesUrls.add(site.getName());
            }
            for (int i = 0; i < urls.size(); ++i) {
                if (url.contains(urls.get(i))) {
                    String mainPage = urls.get(i);
                    Site site = new Site();

                    site.setUrl(mainPage);
                    site.setStatusTime(new Date());
                    site.setStatus(Status.INDEXING);
                    site.setName(namesUrls.get(i));
                    siteRepository.save(site);
                    HtmlParseService parse = new HtmlParseService(mainPage, site, config, siteRepository, pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }
                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);
                    return true;
                }
            }
        } else {
            for (Site site : siteList) {
                if (url.contains(site.getUrl())) {
                    site.setStatus(Status.INDEXING);
                    siteRepository.save(site);
                    HtmlParseService parse = new HtmlParseService(site.getUrl(), site, config, siteRepository, pageRepository);
                    try {
                        parse.addPage();
                    } catch (IOException e) {
                        return false;
                    }
                    site.setStatus(Status.INDEXED);
                    siteRepository.save(site);
                    return true;
                }
            }
        }
        return false;
    }
}
