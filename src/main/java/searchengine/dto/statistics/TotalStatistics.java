package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;
}
