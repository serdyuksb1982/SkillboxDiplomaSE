package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    private TotalStatistics getTotalStatistics() {
        long sites = siteRepository.count();
        long pages = pageRepository.count();
        long lemmas = lemmaRepository.count();
        return new TotalStatistics(sites, pages, lemmas, true);
    }

    private DetailedStatisticsItem getDetailedFromDetailedStatisticItem(SiteModel site) {
        String url = site.getUrl();
        String name = site.getName();
        String status = site.getStatus().toString();
        Date statusTime = site.getStatusTime();
        String error = site.getLastError();
        long pages = pageRepository.countBySiteId(site);
        long lemmas = lemmaRepository.countBySiteModelId(site);
        return new DetailedStatisticsItem(url, name, status, statusTime, error, pages, lemmas);
    }

    private List<DetailedStatisticsItem> getDetailedStatisticsItemList() {
        List<SiteModel> siteList = siteRepository.findAll();
        List<DetailedStatisticsItem> result = new ArrayList<>();
        for (SiteModel site : siteList) {
            DetailedStatisticsItem item = getDetailedFromDetailedStatisticItem(site);
            result.add(item);
        }
        return result;
    }

    public StatisticsResponse getStatisticsResponse() {
        TotalStatistics total = getTotalStatistics();
        List<DetailedStatisticsItem> list = getDetailedStatisticsItemList();
        return new StatisticsResponse(true, new StatisticsData(total, list));
    }
}
