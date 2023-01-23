package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexModel, Long> {
    @Query(value = "SELECT i.* FROM Words_index i WHERE i.lemma_id IN :lemmas AND i.page_id IN :pages", nativeQuery = true)
    List<IndexModel> findByPagesAndLemmas(@Param("lemmas") List<LemmaModel> lemmaListId,
                                          @Param("pages") List<PageModel> pageListId);
}
