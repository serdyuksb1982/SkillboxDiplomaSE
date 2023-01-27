package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface IndexRepository extends JpaRepository<IndexModel, Long> {

    @Query(value = "select i.* from Words_index i where i.lemma_id in :lemmas and i.page_id in :pages", nativeQuery = true)
    List<IndexModel> findByPageAndLemmas(@Param("lemmas") List<LemmaModel> lemmaList,
                                         @Param("pages") List<PageModel> foundPageList);

}
