package searchengine.controllers;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.services.IndexingService;

@RequestMapping
@RestController
public class IndexController {
    @Autowired
    private IndexingService indexingService;

    @ApiOperation("Start parsing web")
    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        boolean isStarted = indexingService.startIndexing();
        JSONObject response = new JSONObject();
        try {
            if(isStarted) {
                response.put("result", false);
                response.put("error", "Parsing started!");
            } else {
                response.put("result", true);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @ApiOperation("Stop parsing web")
    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {
        boolean isStopped = indexingService.stopIndexing();

        JSONObject response = new JSONObject();
        try {
            if (isStopped) {
                response.put("result", false);
                response.put("error", "Parsing stopped!");
            } else {
                response.put("result", true);

            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}
