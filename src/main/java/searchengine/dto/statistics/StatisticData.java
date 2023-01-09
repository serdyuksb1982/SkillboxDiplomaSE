package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter@Setter
public class StatisticData {
    private boolean result;
    private Statistic statistics;
}
