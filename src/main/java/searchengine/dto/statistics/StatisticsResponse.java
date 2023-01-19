package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
public class StatisticsResponse {
    private boolean result;
    private StatisticsData statistics;
}
