package searchengine.controllers;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import searchengine.dto.response.ResultDTO;
import searchengine.exception.ErrorMessage;
import searchengine.exception.IndexException;
import searchengine.exception.InternalServerError;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ResultDTO> nullPointerException(NullPointerException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResultDTO(false, exception.getMessage()));
    }

    @ExceptionHandler(IndexException.class)
    public ResponseEntity<ResultDTO> handlerInterruptedException(IndexException exception) {
        return new ResponseEntity<>(new ResultDTO(true, exception.getMessage()), HttpStatus.OK);
    }

    @ExceptionHandler(InternalServerError.class)
    public ResponseEntity<ErrorMessage> internalServerError(Exception e, WebRequest request) {
        var errors =
                new ErrorMessage(500, new Date(),
                        e.getMessage(), "Internal Server Error");
        return new ResponseEntity<>
                (errors, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
