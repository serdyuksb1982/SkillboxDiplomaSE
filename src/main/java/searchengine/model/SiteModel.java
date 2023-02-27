package searchengine.model;

import lombok.*;
import searchengine.model.enums.Status;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
public class SiteModel implements Serializable {

    /*● id INT NOT NULL AUTO_INCREMENT;
    ● status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL — текущий
    статус полной индексации сайта, отражающий готовность поискового
    движка осуществлять поиск по сайту — индексация или переиндексация
    в процессе, сайт полностью проиндексирован (готов к поиску) либо его не
    удалось проиндексировать (сайт не готов к поиску и не будет до
            устранения ошибок и перезапуска индексации);
    ● status_time DATETIME NOT NULL — дата и время статуса (в случае
    статуса INDEXING дата и время должны обновляться регулярно при
        добавлении каждой новой страницы в индекс);
    ● last_error TEXT — текст ошибки индексации или NULL, если её не было;
    ● url VARCHAR(255) NOT NULL — адрес главной страницы сайта;
    ● name VARCHAR(255) NOT NULL — имя сайта.*/

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    private String url;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteId", cascade = CascadeType.ALL)
    private List<PageModel> pageModelList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteModelId", cascade = CascadeType.ALL)
    private List<LemmaModel> lemmaModelList = new ArrayList<>();

    public SiteModel(Status status,
                     Date statusTime,
                     String lastError,
                     String url,
                     String name,
                     List<PageModel> pageModelList,
                     List<LemmaModel> lemmaModelList) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
        this.pageModelList = pageModelList;
        this.lemmaModelList = lemmaModelList;
    }

    public SiteModel() {
    }
}