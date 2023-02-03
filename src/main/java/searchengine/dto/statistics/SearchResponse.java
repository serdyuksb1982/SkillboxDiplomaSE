package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import searchengine.dto.SearchDto;

import java.util.List;

@Getter@Setter
@AllArgsConstructor
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchDto> data;

    public SearchResponse(boolean result){
        this.result = result;
    }
}
