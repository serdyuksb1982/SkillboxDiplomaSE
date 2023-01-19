package searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class IndexDto {
    private Long pageId;
    private Long lemmaId;
    private Float rank;
}
