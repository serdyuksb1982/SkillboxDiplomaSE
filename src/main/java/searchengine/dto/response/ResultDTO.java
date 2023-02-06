package searchengine.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultDTO {

    private boolean result;

    private String error;

    public ResultDTO(boolean result) {
        this.result = result;
    }

    public ResultDTO(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}

