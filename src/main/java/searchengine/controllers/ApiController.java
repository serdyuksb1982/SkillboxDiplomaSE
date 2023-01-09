package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticData;
import searchengine.services.StatisticService;

@RestController
@RequestMapping("/api")
public class ApiController {

    //private final StatisticsServiceImpl statisticsService;
    private final StatisticService statisticsService;

    public ApiController(StatisticService service) {
        this.statisticsService = service;
    }


    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticData> getStatistics() {
        return ResponseEntity.ok().body(statisticsService.getStatistics());
    }
}
