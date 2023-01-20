package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import java.util.List;


@Repository
public interface PageRepository extends JpaRepository<PageModel, Long> {
    long countBySiteId(SiteModel siteId);

    Iterable<PageModel> findBySiteId(SiteModel sitePath);

    @Query(value = "select p.* from Page p join words_index i on p.id = i.page_id where i.lemma_id in :lemmas", nativeQuery = true)
    List<PageModel> findByLemma(@Param("lemmas")List<LemmaModel> lemma);

}
