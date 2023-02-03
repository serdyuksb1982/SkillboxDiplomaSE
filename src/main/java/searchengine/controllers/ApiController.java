package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.SearchDto;
import searchengine.dto.statistics.ResponseDto;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import searchengine.search.SearchStarter;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SiteRepository siteRepository;
    private final SearchStarter searchStarter;
    public ApiController(StatisticsService statisticsService,
                         IndexingService indexingService,
                         SiteRepository siteRepository,
                         SearchStarter searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.siteRepository = siteRepository;
        this.searchStarter = searchService;
    }

    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatisticsResponse());
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResponseEntity<Object> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResponseEntity<Object> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @ApiOperation("Index pages")
    @PostMapping("/indexPage")
    public ResponseEntity<Object> indexPage(@RequestParam(name = "url") String url) {
        if (url.isEmpty()) return new ResponseEntity<>( new ResponseDto(false, "Error"), HttpStatus.BAD_REQUEST);
         else return new ResponseEntity<>(indexingService.urlIndexing(url), HttpStatus.OK);
    }

    @ApiOperation("Search in sites")
    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0") int offset)
    {

            List<SearchDto> searchData;
            if (!site.isEmpty()) {
                if (siteRepository.findByUrl(site) == null) {

                    return new ResponseEntity<>(new ResponseDto(false, "Указанная страница не найдена"), HttpStatus.BAD_REQUEST);
                } else {
                    searchData = searchStarter.getSearchFromOneSite(query, site, offset, 30);
                }
            } else {
                searchData = searchStarter.getFullSearch(query, offset, 30);
            }
            return new ResponseEntity<>(new SearchResponse(true, searchData.size(), searchData), HttpStatus.OK);

    }
}
