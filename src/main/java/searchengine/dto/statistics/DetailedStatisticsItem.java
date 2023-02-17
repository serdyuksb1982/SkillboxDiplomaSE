package searchengine.dto.statistics;

import java.util.Date;

public record DetailedStatisticsItem(String url, String name, String status, Date statusTime, String error, long pages, long lemmas) {

}
