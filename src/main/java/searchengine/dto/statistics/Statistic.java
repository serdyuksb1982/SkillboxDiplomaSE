package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import searchengine.model.Site;


import java.util.List;

@Data
@Getter@Setter
public class Statistic {
    private TotalStatistics total;
    private List<Site> detailed;
    public void addDetailed(Site site) {
        detailed.add(site);
    }
}
