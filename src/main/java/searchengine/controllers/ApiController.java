package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticData;
import searchengine.services.IndexPageService;
import searchengine.services.IndexingService;
import searchengine.services.StatisticService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final IndexingService indexingService;
    private final StatisticService statisticsService;

    private final IndexPageService indexPageService;

    public ApiController(IndexingService indexingService, StatisticService service, IndexPageService indexPageService) {
        this.indexingService = indexingService;
        this.statisticsService = service;
        this.indexPageService = indexPageService;
    }

    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticData> getStatistics() {
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
    @GetMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(@RequestParam(name = "url") String url) {
        return ResponseEntity.ok(indexPageService.indexPage(url));
    }
}
