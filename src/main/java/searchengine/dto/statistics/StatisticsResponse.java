package searchengine.dto.statistics;

import lombok.*;

@Data
@AllArgsConstructor
public class StatisticsResponse {
     private boolean result;
     private StatisticsData statistics;
}
