package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "words_index", indexes = {@Index(
        name = "page_id_list", columnList = "page_id"),
        @Index(name = "lemma_id_list", columnList = "lemma_id")})
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class IndexEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private PageEntity page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private LemmaEntity lemma;

    @Column(nullable = false, name = "index_rank")
    private float rank;

}