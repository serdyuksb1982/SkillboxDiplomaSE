package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                StatisticsServiceImpl.this.indexing();
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

        TotalStatistics total = new TotalStatistics();
        total.setPages(allPages.get());
        total.setSites(allSites.get());
        statistic.setTotal(total);
        StatisticsResponse statistics = new StatisticsResponse();
        statistics.setResult(true);
        statistics.setStatistics(statistic);
        return statistics;
    }

    /*@Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {"Ошибка индексации: главная страница сайта не доступна", "Ошибка индексации: сайт не доступен", ""};
        TotalStatistics total = new TotalStatistics();
        total.setSites(config.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = config.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = random.nextInt(1_000);
            int lemmas = pages * random.nextInt(1_000);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(statuses[i % 3]);
            item.setError(errors[i % 3]);
            item.setStatusTime(System.currentTimeMillis() -
                    (random.nextInt(10_000)));
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }*/

}
