package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.SiteEntity;


import java.util.List;

@Data
@Getter@Setter
public class StatisticsData {

    private TotalStatistics total;
    private List<SiteEntity> detailed;

}
