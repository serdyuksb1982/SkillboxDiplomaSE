package searchengine.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.response.ResultDTO;
import searchengine.dto.SearchDto;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;
import searchengine.search.SearchStarter;

import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public record ApiController(StatisticsService statisticsService, IndexingService indexingService, SiteRepository siteRepository, SearchStarter searchStarter) {

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

    @PostMapping("/indexPage")
    @ApiOperation("Индексация отдельной страницы")
    public ResultDTO indexPage(@RequestParam(name = "url") String url) {
        if (url.isEmpty()) {
            log.info("Страница не указана");
            return new ResultDTO(false, "Страница не указана", HttpStatus.BAD_REQUEST);
        } else {
            if (indexingService.indexPage(url) == true) {
                log.info("Страница - " + url + " - добавлена на переиндексацию");
                return new ResultDTO(true, HttpStatus.OK);
            } else {
                log.info("Указанная страница" + "за пределами конфигурационного файла");
                return new ResultDTO(false, "Указанная страница" + "за пределами конфигурационного файла", HttpStatus.BAD_REQUEST);
            }
        }
    }



    @ApiOperation("Search in sites")
    @GetMapping("/search")
    public ResultDTO search(@RequestParam(name = "query", required = false, defaultValue = "") String query,
                                            @RequestParam(name = "site", required = false, defaultValue = "") String site,
                                            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset) {
        List<SearchDto> searchData;
        if (!site.isEmpty()) {
            if (siteRepository.findByUrl(site) == null) {

                return new ResultDTO(false, "Данная страница находится за пределами сайтов,\n" +
                        "указанных в конфигурационном файле", HttpStatus.BAD_REQUEST) ;
            } else {
                searchData = searchStarter.getSearchFromOneSite(query, site, offset, 30);
            }
        } else {
            searchData = searchStarter.getFullSearch(query, offset, 30);
        }
        return new ResultDTO(true, searchData.size(), searchData, HttpStatus.OK);
    }
}
