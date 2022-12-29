package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.dto.statistics.emum.Status;
import searchengine.dto.statistics.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    private List<Thread> threads = new ArrayList<>();

    private List<ForkJoinPool> forkJoinPools = new ArrayList<>();
    private final SitesList config;

    public StatisticsServiceImpl(PageRepository pageRepository, SiteRepository siteRepository, SitesList config) {
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.config = config;
    }

    private SitesParseService startParse(SiteModel site) {
        return new SitesParseService(site.getUrl(), site, config, siteRepository, pageRepository);
    }

    public void indexing() {
        threads = new ArrayList<>();
        forkJoinPools = new ArrayList<>();

        pageRepository.deleteAll();
        siteRepository.deleteAll();

        List<SitesParseService> sitesParses = new ArrayList<>();
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

            sitesParses.add(startParse(site));
            siteRepository.save(site);
        }
        urls.clear();
        namesUrl.clear();

        for (SitesParseService parse : sitesParses) {
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
        new Thread(() -> indexing()).start();
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
    @Override
    public StatisticsResponse getStatistics() {
        //TODO доработать метод, не работает правильно пока
        StatisticsData statistic = new StatisticsData();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();
        List<SiteModel> siteList = siteRepository.findAll();
        if (siteList.size() == 0) return new StatisticsResponse();
        for (SiteModel it : siteList) {
            int pages = pageRepository.countBySite(it);
            it.setPages(pages);
            statistic.getDetailed().add(it);
            allPages.updateAndGet((int v) -> v + pages);
            allSites.getAndIncrement();
        }
        StatisticsResponse statistics = getStatisticsResponse(statistic, allPages, allSites);        return statistics;
    }

    private StatisticsResponse getStatisticsResponse(StatisticsData statistic,
                                                     AtomicInteger allPages,
                                                     AtomicInteger allSites) {
        TotalStatistics total = new TotalStatistics();
        total.setPages(allPages.get());
        total.setSites(allSites.get());
        statistic.setTotal(total);
        StatisticsResponse statistics = new StatisticsResponse();
        statistics.setResult(true);
        statistics.setStatistics(statistic);
        return statistics;
    }
}
