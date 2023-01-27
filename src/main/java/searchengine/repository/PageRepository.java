package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;


@Repository
@Transactional
public interface PageRepository extends JpaRepository<PageModel, Long> {
    long countBySiteId(SiteModel siteId);

    Iterable<PageModel> findBySiteId(SiteModel sitePath);
    @Query(value = "SELECT p.* FROM Page p JOIN Words_index i ON p.id = i.page_id WHERE i.lemma_id IN :lemmas", nativeQuery = true)
    List<PageModel> findByLemmaList(@Param("lemmas") Collection<LemmaModel> lemmaListId);
}
