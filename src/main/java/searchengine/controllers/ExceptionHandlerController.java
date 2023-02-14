package searchengine.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import searchengine.dto.response.ResultDTO;
import searchengine.exception.CurrentRuntimeException;

@RestControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResultDTO> nullPointerException(NullPointerException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResultDTO(false, exception.getMessage()));
    }

    @ExceptionHandler(CurrentRuntimeException.class)
    public ResponseEntity<ResultDTO> handlerInterruptedException(CurrentRuntimeException exception) {
        return new ResponseEntity<>(new ResultDTO(true, exception.getMessage()), HttpStatus.OK);
    }
}
