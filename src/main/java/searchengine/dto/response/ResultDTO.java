package searchengine.dto.response;

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.SearchDto;

import java.util.List;

@Getter
@Setter
public class ResultDTO {

    private boolean result;

    private String error;

    private int count;

    private List<SearchDto> data;

    public ResultDTO(boolean result) {
        this.result = result;
    }

    public ResultDTO(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public ResultDTO(boolean result, int count, List<SearchDto> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }
}

