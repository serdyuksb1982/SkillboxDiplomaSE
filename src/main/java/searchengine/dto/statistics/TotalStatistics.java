package searchengine.dto.statistics;

import lombok.*;

@Value
public class TotalStatistics {
    Long sites;
    Long pages;
    Long lemmas;
    boolean indexing;
}
