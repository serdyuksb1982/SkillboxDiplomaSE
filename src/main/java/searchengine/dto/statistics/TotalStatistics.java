package searchengine.dto.statistics;

import lombok.*;

@Data
@AllArgsConstructor
public class TotalStatistics {
    private Long sites;
    private Long pages;
    private Long lemmas;
    private boolean indexing;
}
