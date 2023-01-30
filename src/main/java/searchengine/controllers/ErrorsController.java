package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.statistics.ResponseDto;

@RestControllerAdvice
public class ErrorsController {

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResponseDto> currentErrorException(NullPointerException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDto("Данные поискового запроса" + " отсутствуют в базе " + ex.getMessage()));
    }
}
