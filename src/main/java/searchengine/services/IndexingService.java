package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteModel;
import searchengine.model.enums.Status;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.index.WebParser;
import searchengine.services.lemma.LemmaIndexer;
import searchengine.services.site.SiteIndexed;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {
    private ExecutorService executorService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaIndexer lemmaIndexer;
    private final WebParser webParser;
    private final SitesList config;

    public boolean startIndexing() {
        if (isIndexingActive()) {
            log.debug("Indexing is already running.");
            return false;
        } else {
            List<Site> siteList = config.getSites();
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (Site site : siteList) {
                //Optional<String> url = Optional

                String url = site.getUrl();
                SiteModel siteModel = new SiteModel();
                siteModel.setName(site.getName());
                log.info("Indexing web site ".concat(site.getName()));
                executorService.submit(new SiteIndexed(pageRepository,
                        siteRepository,
                        lemmaRepository,
                        indexRepository,
                        lemmaIndexer,
                        webParser,
                        url,
                        config));
            }
            executorService.shutdown();
        }
        return true;
    }


    public boolean stopIndexing() {
        if (isIndexingActive()) {
            log.info("Index stopping.");
            executorService.shutdownNow();
            return true;
        } else {
            return false;
        }
    }

    private boolean isIndexingActive() {
        siteRepository.flush();
        Iterable<SiteModel> siteList = siteRepository.findAll();
        for (SiteModel site : siteList) {
            if (site.getStatus() == Status.INDEXING) {
                return true;
            }
        }
        return false;
    }

    public boolean urlIndexing(String url) {
        if (urlCheck(url)) {
            log.info("Начата переиндексация сайта - " + url);
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            executorService.submit(new SiteIndexed(pageRepository,
                    siteRepository,
                    lemmaRepository,
                    indexRepository,
                    lemmaIndexer,
                    webParser,
                    url,
                    config));
            executorService.shutdown();
            return true;
        } else {
            return false;
        }
        
    }

    private boolean urlCheck(String url) {
        List<Site> urlList = config.getSites();
        for (Site site : urlList) {
            if (site.getUrl().equals(url)) {
                return true;
            }
        }
        return false;
    }

}
