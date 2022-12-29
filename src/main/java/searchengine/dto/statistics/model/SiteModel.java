package searchengine.dto.statistics.model;

import lombok.*;
import searchengine.dto.statistics.emum.Status;

import javax.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "site")
@Getter
@Setter
@Data
public class SiteModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.ORDINAL)
    private Status status;

    @Column(name = "status_time", nullable = false)
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    private Integer pages;

}