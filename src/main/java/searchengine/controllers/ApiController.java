package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticServiceImpl;

@RestController
@RequestMapping("/api")
public class ApiController {

    //private final StatisticsServiceImpl statisticsService;
    private final StatisticServiceImpl statisticsService;

    public ApiController(StatisticServiceImpl initParserService) {
        this.statisticsService = initParserService;
    }

    /*public ApiController(StatisticsServiceImpl service) {
        this.statisticsService = service;
    }*/


    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
