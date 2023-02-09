package searchengine.exception;

import java.util.Date;

public record ErrorMessage(Integer statusCode,
                           Date timestamp,
                           String message,
                           String description) {
}
