package searchengine.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class IndexDto {
    private long pageID;
    private long lemmaID;
    private float rank;
}
