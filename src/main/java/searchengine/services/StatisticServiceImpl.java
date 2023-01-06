package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StatisticServiceImpl implements StatisticsService {
    private SiteRepository siteRepository;

    private PageRepository pageRepository;

    private StatisticServiceImpl(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
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

}
