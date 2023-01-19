package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "lemma", indexes = {@Index(name = "lemma_list", columnList = "lemma")})
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class LemmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private SiteEntity siteEntityId;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<IndexEntity> index = new ArrayList<>();

    private String lemma;

    private int frequency;

    public LemmaEntity(String lemma, int frequency, SiteEntity siteEntityId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteEntityId = siteEntityId;
    }

}
