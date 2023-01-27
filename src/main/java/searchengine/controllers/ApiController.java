package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.SearchDto;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import searchengine.services.search.SearchService;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    private final SiteRepository siteRepository;

    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SiteRepository siteRepository, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
        this.searchService = searchService;
    }

    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResponseEntity<Boolean> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResponseEntity<Boolean> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @ApiOperation("Index pages")
    @PostMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            log.info("Страница не указана");
            return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
        } else {
            log.info("Страница - " + url + " - добавлена на переиндексацию");
            return new ResponseEntity<>(indexingService.urlIndexing(url), HttpStatus.OK);
        }
    }

    @GetMapping("/search")
    @ApiOperation("Поиск информации")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "start", required = false, defaultValue = "0") int start,
                                         @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        if (query.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            List<SearchDto> searchData;
            if (!site.isEmpty()) {
                if (siteRepository.findByUrl(site) == null) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                } else {
                    searchData = searchService.siteSearch(query, site, start, limit);
                }
            } else {
                searchData = searchService.fullSiteSearch(query, start, limit);
            }

            return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);
        }
    }
}
