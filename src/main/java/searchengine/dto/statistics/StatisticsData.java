package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.SiteEntity;


import java.util.List;

@Data
@Getter@Setter
@AllArgsConstructor
public class StatisticsData {

    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;

}
