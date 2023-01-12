package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.SiteConfiguration;
import searchengine.config.SitesList;
import searchengine.model.enums.Status;
import searchengine.model.Site;
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
    private SitesList sitesListConfig;

    public IndexingService(PageRepository pageRepository, SiteRepository siteRepository, SitesList sitesList) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.sitesListConfig = sitesList;
    }

    private HtmlParseService initParseWebSite(Site site) {
        HtmlParseService htmlParseService = new HtmlParseService(site.getUrl(), site, sitesListConfig, siteRepository, pageRepository);
        return htmlParseService;
    }

    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        pageRepository.deleteAll();
        siteRepository.deleteAll();

        List<HtmlParseService> sitesParses = new ArrayList<>();
        List<SiteConfiguration> sites = sitesListConfig.getSites();

        List<String> urls = new ArrayList<>();
        for (SiteConfiguration site : sites) {
            urls.add(site.getUrl());
        }

        List<String> namesUrl = new ArrayList<>();
        for (SiteConfiguration site : sites) {
            namesUrl.add(site.getName());
        }

        for (int i = 0; i < urls.size(); i++) {
            String currentSitePage = urls.get(i);

            Site site = siteRepository.findSiteByUrl(currentSitePage);
            if (site == null) {
                site = new Site();
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
                    Site site = parse.getSite();

                    try {
                        site.setStatus(Status.INDEXING);
                        siteRepository.save(site);
                        ForkJoinPool tasksForThreads = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
                        System.out.println(tasksForThreads + " Treads");
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

    public boolean startIndexing() {

        AtomicBoolean isStartedIndexing = new AtomicBoolean(false);

        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING)) {
                isStartedIndexing.set(true);
            }
        }
        if (isStartedIndexing.get()) {
            return true;
        }
        new Thread(this::indexing).start();
        System.out.println(Thread.activeCount() + " Threads ");

        return false;
    }


    public boolean stopIndexing() {
        System.out.println("Потоков работает: " + threads.size());
        AtomicBoolean isIndexing = new AtomicBoolean(false);
        for (Site site : siteRepository.findAll()) {
            if (site.getStatus().equals(Status.INDEXING) || site.getStatus().equals(Status.INDEXED) || site.getStatus().equals(Status.FAILED)) {
                isIndexing.set(true);
            }
        }
        if (!isIndexing.get()) {
            return true;
        }
        for (ForkJoinPool forkJoinPool : forkJoinPools) {
            forkJoinPool.shutdownNow();
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Site site : siteRepository.findAll()) {
            site.setLastError("Остановка индексации");
            site.setStatus(Status.FAILED);
            siteRepository.save(site);
        }
        threads.clear();
        forkJoinPools.clear();
        return false;
    }
}