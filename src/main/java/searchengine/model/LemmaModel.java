package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "lemma")
public class LemmaModel implements Serializable {

    /*id INT NOT NULL AUTO_INCREMENT;
    site_id INT NOT NULL — ID веб-сайта из таблицы site;
    lemma VARCHAR(255) NOT NULL — нормальная форма слова (лемма);
    frequency INT NOT NULL — количество страниц, на которых слово
    встречается хотя бы один раз. Максимальное значение не может
    превышать общее количество слов на сайте*/



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private SiteModel siteModelId;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL)
    private List<IndexModel> index = new ArrayList<>();

    private String lemma;

    private int frequency;

    public LemmaModel(String lemma, int frequency, SiteModel siteModelId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteModelId = siteModelId;
    }

    public LemmaModel() {
    }
}
