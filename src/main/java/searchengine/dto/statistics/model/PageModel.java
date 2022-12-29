package searchengine.dto.statistics.model;

import lombok.*;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "page", indexes = @Index(name = "pathPage", columnList = "path"))
@Getter
@Setter
@Data
public class PageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "path", nullable = false, unique = true, columnDefinition = "VARCHAR(256)")
    private String path;

    @Column(name = "code", nullable = false)
    private int code;

    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "site_ID")
    private SiteModel site;


}
