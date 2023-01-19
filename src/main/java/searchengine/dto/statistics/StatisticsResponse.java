package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
@AllArgsConstructor
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
