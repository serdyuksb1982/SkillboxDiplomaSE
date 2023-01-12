package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.statistics.Statistic;
import searchengine.dto.statistics.StatisticData;
import searchengine.dto.statistics.TotalStatistics;

import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatisticService {
    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private StatisticService(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    public StatisticData getStatistics() {

        Statistic statistic = new Statistic();
        AtomicInteger allPages = new AtomicInteger();
        AtomicInteger allSites = new AtomicInteger();
        List<Site> siteList = siteRepository.findAll();

        if (siteList.size() == 0) return new StatisticData();
        siteList.forEach(it -> {
            int pages = pageRepository.countBySite(it);


            it.setPages(pages);

            statistic.addDetailed(it);

            allPages.updateAndGet(v -> v + pages);

            allSites.getAndIncrement();
        });

        TotalStatistics total = new TotalStatistics();
        total.setIndexing(true);
        total.setPages(allPages.get());
        total.setSites(allSites.get());
        statistic.setTotal(total);
        StatisticData statistics = new StatisticData();
        statistics.setResult(true);
        statistics.setStatistics(statistic);
        return statistics;
    }

}
