package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.response.ResultDTO;
import searchengine.dto.SearchDto;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import searchengine.search.SearchStarter;

import java.util.ArrayList;
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
    public ResultDTO startIndexing() {
        return indexingService.startIndexing();
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResultDTO stopIndexing() {
        log.info("ОСТАНОВКА ИНДЕКСАЦИИ");
        return indexingService.stopIndexing();
    }

    @ApiOperation("Index pages")
    @PostMapping("/indexPage")
    public ResponseEntity<ResultDTO> indexPage(@RequestParam(name = "url") String url) {
        System.out.println("1");
        if (url.isEmpty()) return new ResponseEntity<>(new ResultDTO(false, "BAD_REQUEST"), HttpStatus.BAD_REQUEST);
        else if (indexingService.urlIndexing(url) == true){
            System.out.println("2");
            return new ResponseEntity<>(new ResultDTO(true), HttpStatus.OK);
        } else {
            log.info("Указанная страница" + "за пределами конфигурационного файла");
            return new ResponseEntity<>(new ResultDTO(false, "Указанная страница" + "за пределами конфигурационного файла"), HttpStatus.BAD_REQUEST);
        }
    }



    @ApiOperation("Search in sites")
    @GetMapping("/search")
    public ResponseEntity<ResultDTO> search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                         @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                         @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {

        List<SearchDto> searchData;
        if (!site.isEmpty()) {
            if (siteRepository.findByUrl(site) == null) {

                return new ResponseEntity<>(new ResultDTO(false, "Данная страница находится за пределами сайтов,\n" +
                        "указанных в конфигурационном файле"), HttpStatus.BAD_REQUEST);
            } else {
                searchData = searchStarter.getSearchFromOneSite(query, site, offset, 30);
            }
        } else {
            searchData = searchStarter.getFullSearch(query, offset, 30);
        }
        return new ResponseEntity<>(new ResultDTO(true, searchData.size(), searchData), HttpStatus.OK);

    }
}
