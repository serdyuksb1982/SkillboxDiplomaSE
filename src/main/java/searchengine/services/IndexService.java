package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.enums.Status;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.index.Index;
import searchengine.services.lemma.LemmaIndex;
import searchengine.services.site.SiteIndex;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service

@Slf4j
public class IndexService {
    private ExecutorService executorService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaIndex lemmaParser;
    private final Index indexParser;
    private final SitesList sitesList;

    public IndexService(PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository, LemmaIndex lemmaParser, Index indexParser, SitesList sitesList) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaParser = lemmaParser;
        this.indexParser = indexParser;
        this.sitesList = sitesList;
    }


    /*public boolean urlIndexing(String url) {
        if (urlCheck(url)) {
            log.info("Start indexing site name: " + url);
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            executorService.submit(new SiteIndex(pageRepository, siteRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList));
            executorService.shutdown();

            return true;
        } else {
            return false;
        }
    }*/

    public boolean indexingAll() {
        if (isIndexingStatus()) {
            log.debug("Indexing begin!");
            return false;
        } else {
            List<Site> siteList = sitesList.getSites();
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (Site site : siteList) {
                String url = site.getUrl();
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setName(site.getName());
                log.info("Site index: " + site.getName());
                executorService.submit(new SiteIndex(pageRepository, siteRepository, lemmaRepository, indexRepository, lemmaParser, indexParser, url, sitesList));
            }
            executorService.shutdown();
        }
        return true;
    }

    public boolean stopIndexing() {
        if (isIndexingStatus()) {
            log.info("Indexing stopped!");
            executorService.shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    private boolean isIndexingStatus() {
        siteRepository.flush();
        Iterable<SiteEntity> siteList = siteRepository.findAll();
        for (SiteEntity site : siteList) {
            if (site.getStatus() == Status.INDEXING) {
                return true;
            }
        }
        return false;
    }

    private boolean urlCheck(String url) {
        List<Site> urlList = sitesList.getSites();
        for (Site site : urlList) {
            if (site.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }
}
