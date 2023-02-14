package searchengine.services.site;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.IndexDto;
import searchengine.dto.LemmaDto;
import searchengine.dto.PageDto;
import searchengine.exception.CurrentInterruptedException;
import searchengine.exception.CurrentRuntimeException;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.enums.Status;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.index.WebParser;
import searchengine.services.lemma.LemmaIndexer;
import searchengine.services.pageconvertor.PageIndexer;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

@RequiredArgsConstructor
@Slf4j
public class SiteIndexed implements Runnable {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaIndexer lemmaIndexer;
    private final WebParser webParser;
    private final String url;
    private final SitesList config;

    /**
     * This is method start indexing sites, and set in model...
     */
    @Override
    public void run() {
        if (siteRepository.findByUrl(url) != null) {
            log.info("start site date delete from ".concat(url));
            SiteModel site = siteRepository.findByUrl(url);
            site.setStatus(Status.INDEXING);
            site.setName(getSiteName());
            site.setStatusTime(new Date());
            siteRepository.save(site);
            siteRepository.flush();
            siteRepository.delete(site);
        }
        log.info("Site indexing start ".concat(url).concat(" ").concat(getSiteName()) );
        SiteModel site = new SiteModel();
        site.setUrl(url);
        site.setName(getSiteName());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        siteRepository.flush();
        siteRepository.save(site);
        try {
            if (!Thread.interrupted()) {
                List<PageDto> pageDtoList;
                if (!Thread.interrupted()) {
                    String urls = url.concat("/") ;
                    List<PageDto> pageDtosList = new CopyOnWriteArrayList<>();
                    List<String> urlList = new CopyOnWriteArrayList<>();
                    ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                    List<PageDto> pages = forkJoinPool.invoke(new PageIndexer(urls, pageDtosList, urlList, config));
                    pageDtoList = new  CopyOnWriteArrayList<>(pages);
                } else throw new CurrentInterruptedException("Fork join exception.");
                List<PageModel> pageList = new CopyOnWriteArrayList<>();

                for (PageDto page : pageDtoList) {
                    int start = page.url().indexOf(url) + url.length();
                    String pageFormat = page.url().substring(start);
                    pageList.add(new PageModel(site, pageFormat, page.code(),
                            page.content()));
                }
                pageRepository.flush();
                pageRepository.saveAll(pageList);
            } else {
                throw new CurrentInterruptedException("Local interrupted exception.");
            }

            if (!Thread.interrupted()) {
                SiteModel siteModel = siteRepository.findByUrl(url);
                siteModel.setStatusTime(new Date());
                lemmaIndexer.startLemmaIndexer();
                List<LemmaDto> lemmaDtoList = lemmaIndexer.getLemmaDtoList();
                List<LemmaModel> lemmaList = new CopyOnWriteArrayList<>();

                for (LemmaDto lemmaDto : lemmaDtoList) {
                    lemmaList.add(new LemmaModel(lemmaDto.lemma(), lemmaDto.frequency(), siteModel));
                }
                lemmaRepository.flush();
                lemmaRepository.saveAll(lemmaList);
            } else {
                throw new CurrentInterruptedException("Invalid lemmas writer!");
            }

            if (!Thread.interrupted()) {
                webParser.startWebParser(site);
                List<IndexDto> indexDtoList = new CopyOnWriteArrayList<>(webParser.getConfig());
                List<IndexModel> indexModels = new CopyOnWriteArrayList<>();
                site.setStatusTime(new Date());
                for (IndexDto indexDto : indexDtoList) {
                    PageModel page = pageRepository.getById(indexDto.pageID());
                    LemmaModel lemma = lemmaRepository.getById(indexDto.lemmaID());
                    indexModels.add(new IndexModel(page, lemma, indexDto.rank()));
                }
                indexRepository.flush();
                indexRepository.saveAll(indexModels);
                log.info("WebParser stopping ".concat(url));
                site.setStatusTime(new Date());
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);

            } else {
                throw new CurrentInterruptedException("Current site indexing exception");
            }

        } catch (InterruptedException e) {
            log.error("WebParser stopped from ".concat(url).concat(". ").concat(e.getMessage()));
            SiteModel sites = new SiteModel();
            sites.setLastError("WebParser stopped");
            sites.setStatus(Status.FAILED);
            sites.setStatusTime(new Date());
            siteRepository.save(site);
            new CurrentInterruptedException("Interrupted exception");
        }
    }

    private String getSiteName() {
        List<Site> sites = config.getSites();
        for (Site site : sites) {
            if (site.getUrl().equals(url)) {
                return site.getName();
            }
        }
        return "";
    }
}

