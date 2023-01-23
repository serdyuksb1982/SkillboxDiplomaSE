package searchengine.dto;

import lombok.*;

@Data
@AllArgsConstructor
public class IndexDto {
    private Long pageID;
    private Long lemmaID;
    private Float rank;
}
