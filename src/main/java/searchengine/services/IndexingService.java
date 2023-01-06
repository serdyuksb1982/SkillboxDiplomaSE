package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.enums.Status;
import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private List<Thread> threads = new ArrayList<>();

    private List<ForkJoinPool> forkJoinPools = new ArrayList<>();
    private final SitesList config;

    public IndexingService(PageRepository pageRepository, SiteRepository siteRepository, SitesList config) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.config = config;
    }

    private HtmlParseService initParseWebSite(SiteModel site) {
        return new HtmlParseService(site.getUrl(), site, config, siteRepository, pageRepository);
    }

    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        pageRepository.deleteAll();
        siteRepository.deleteAll();

        List<HtmlParseService> sitesParses = new ArrayList<>();
        List<Site> sites = config.getSites();
        List<String> urls = new ArrayList<>();
        for (Site site : sites) {
            urls.add(site.getUrl());
        }
        List<String> namesUrl = new ArrayList<>();
        for (Site site : sites) {
            namesUrl.add(site.getName());
        }

        for (int i = 0; i < urls.size(); i++) {
            String currentSitePage = urls.get(i);

            SiteModel site = siteRepository.findSiteByUrl(currentSitePage);
            if (site == null) {
                site = new SiteModel();
            }
            site.setUrl(currentSitePage);
            site.setStatusTime(new Date());
            site.setStatus(Status.INDEXING);
            site.setName(namesUrl.get(i));

            sitesParses.add(initParseWebSite(site));
            siteRepository.save(site);
        }
        urls.clear();
        namesUrl.clear();

        for (HtmlParseService parse : sitesParses) {
            threads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    SiteModel site = parse.getSite();

                    try {
                        site.setStatus(Status.INDEXING);
                        siteRepository.save(site);
                        ForkJoinPool tasksForThreads = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                        forkJoinPools.add(tasksForThreads);
                        tasksForThreads.execute(parse);
                        site.setStatus(Status.INDEXED);
                        siteRepository.save(site);
                    } catch (CancellationException exception) {
                        exception.printStackTrace();
                        site.setLastError("Current error: -> ".concat(exception.getMessage()) );
                        site.setStatus(Status.FAILED);
                        siteRepository.save(site);
                    }
                }
            }));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (ForkJoinPool forkJoinPool : forkJoinPools) {
            forkJoinPool.shutdown();
        }
    }

    public boolean startParse() {
        AtomicBoolean isStartedIndexing = new AtomicBoolean(false);

        for (SiteModel site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING)) {
                isStartedIndexing.set(true);
            }
        }
        if (isStartedIndexing.get()) {
            return true;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                IndexingService.this.indexing();
            }
        }).start();
        return false;
    }

    public boolean stopParse() {
        //TODO mutex
        AtomicBoolean isStoppedIndexing = new AtomicBoolean(false);
        for (SiteModel siteModel : siteRepository.findAll()) {
            if (siteModel.getStatus().equals(Status.INDEXING)) {
                isStoppedIndexing.set(true);
            }
        }
        if (!isStoppedIndexing.get()) {
            return true;
        }
        for (ForkJoinPool forkJoinPool : forkJoinPools) {
            forkJoinPool.shutdown();
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (SiteModel site : siteRepository.findAll()) {
            site.setLastError("Inform error message. Stop parsing site: ".concat(site.getName()).concat(", last error: ").concat(site.getLastError()) );
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        }
        threads.clear();
        forkJoinPools.clear();
        return false;
    }

}