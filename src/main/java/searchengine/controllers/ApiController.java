package searchengine.controllers;


import io.swagger.annotations.ApiOperation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;

@RestController
@RequestMapping("/api")
public class ApiController {



    @ApiOperation("Get all statistics")
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return null;
    }

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResponseEntity<Boolean> startIndexing() {
        return null;
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResponseEntity<Boolean> stopIndexing() {
        return null;
    }

    @ApiOperation("Index pages")
    @GetMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(@RequestParam(name = "url") String url) {
        return null;
    }
}
