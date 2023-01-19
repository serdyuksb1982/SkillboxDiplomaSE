package searchengine.controllers;


import io.swagger.annotations.ApiOperation;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexService;
import searchengine.services.StatisticService;

@RestController
@RequestMapping("/api")
@Slf4j
@Tag(name = "API контролер поискового движка", description = "Индексация всех страниц, переиндексация отдельного сайта, " + "остановка индексации, поиск, статистика по сайтам")
public class ApiController {
    private final StatisticService statisticsService;
    private final IndexService indexingService;
    private final SiteRepository siteRepository;

    public ApiController(StatisticService statisticsService, IndexService indexingService, SiteRepository siteRepository) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
    }


    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResponseEntity<Boolean> startIndexing() {
        return ResponseEntity.ok(indexingService.indexingAll());
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResponseEntity<Boolean> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @ApiOperation("Index pages")
    @GetMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(@RequestParam(name = "url") String url) {
        return null;
    }
}
