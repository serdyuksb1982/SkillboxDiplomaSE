package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "words_index")
@EqualsAndHashCode
public class IndexModel implements Serializable {

    /*● id INT NOT NULL AUTO_INCREMENT;
    ● page_id INT NOT NULL — идентификатор страницы;
    ● lemma_id INT NOT NULL — идентификатор леммы;
    ● rank FLOAT NOT NULL — количество данной леммы для данной
    страницы.*/

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private PageModel page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private LemmaModel lemma;

    @Column(nullable = false, name = "index_rank")
    private float rank;

    public IndexModel(PageModel page, LemmaModel lemma, float rank) {
        this.page = page;
        this.lemma = lemma;
        this.rank = rank;
    }

    public IndexModel() {
    }
}