package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

import searchengine.model.enums.Status;

import java.util.Date;

@Data
@AllArgsConstructor
public class DetailedStatisticsItem {
     private String url;
     private String name;
     private Status status;
     private Date statusTime;
     private String error;
     private long pages;
     private long lemmas;
}
