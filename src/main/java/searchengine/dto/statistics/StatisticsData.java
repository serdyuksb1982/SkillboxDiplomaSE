package searchengine.dto.statistics;

import lombok.*;


import java.util.List;

@Data
@AllArgsConstructor
public class StatisticsData {

    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;

}
