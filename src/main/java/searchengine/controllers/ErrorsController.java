package searchengine.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.webjars.NotFoundException;
import searchengine.dto.statistics.ResponseDto;

@RestControllerAdvice
@RequestMapping("/api")
@Slf4j
public class ErrorsController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseDto> handleException(NotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDto(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseDto> mismatchException(MethodArgumentTypeMismatchException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ResponseDto(exception.getMessage()));
    }

}
