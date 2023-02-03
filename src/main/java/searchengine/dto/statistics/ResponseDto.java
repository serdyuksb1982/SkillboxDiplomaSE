package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.SearchDto;

import java.util.List;

@Getter@Setter
public class ResponseDto {
    private boolean result;
    private int count;
    private List<SearchDto>  data;

    private String error;

    public ResponseDto(boolean result, int count, List<SearchDto> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

    public ResponseDto(boolean result) {
        this.result = result;
    }

    public ResponseDto(boolean result, String error) {
        this.result = result;
        this.error = error;
    }

    public ResponseDto(String message) {
        this.error = message;
    }

    public ResponseDto() {
    }
}
