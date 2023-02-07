package searchengine.dto.statistics;

import lombok.*;

@Data
@AllArgsConstructor
public class TotalStatistics {
    private long sites;
    private long pages;
    private long lemmas;
    private boolean indexing;
}
