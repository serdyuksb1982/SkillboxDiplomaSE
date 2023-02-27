package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.ResultDTO;
import searchengine.model.SiteModel;
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

import static searchengine.model.enums.Status.INDEXING;

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

    public ResultDTO startIndexing() {
        if (isIndexingActive()) {
            log.debug("Indexing is already running.");
            new ResultDTO(false, "Индексация уже запущена").getError();

        } else {

            List<Site> siteList = config.getSites();
            executorService = Executors.
                    newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            for (Site site : siteList) {
                String url = site.getUrl();
                SiteModel siteModel = new SiteModel();
                siteModel.setName(site.getName());
                log.info("Indexing web site ".concat(site.getName()));
                executorService.submit(new SiteIndexed(pageRepository, siteRepository, lemmaRepository,
                        indexRepository, lemmaIndexer, webParser, url, config));
            }
            executorService.shutdown();
        }
        return new ResultDTO(true);
    }

    public ResultDTO stopIndexing() {
        if (!isIndexingActive()) {
            log.info("Site indexing is already running!");
            return new ResultDTO(false, "Индексация не запущена");
        } else {
            log.info("Index stopping.");
            executorService.shutdown();
            return new ResultDTO(true);
        }
    }

    private boolean isIndexingActive() {
        siteRepository.flush();
        Iterable<SiteModel> siteList = siteRepository.findAll();
        for (SiteModel site : siteList) {
            if (site.getStatus() == INDEXING) {
                return true;
            }
        }
        return false;
    }

    public boolean indexPage(String urlPage) {

        if (isUrlSiteEquals(urlPage)) {
            log.info("Начата переиндексация сайта - " + urlPage);
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            executorService.submit(new SiteIndexed(pageRepository, siteRepository, lemmaRepository, indexRepository, lemmaIndexer, webParser, urlPage, config));
            executorService.shutdown();
            return true;
        } else {
            return false;
        }

    }

    private boolean isUrlSiteEquals(String url) {
        return config.getSites().stream().anyMatch(site -> site.getUrl().equals(url));
    }

}
