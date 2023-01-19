package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.util.List;


@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
    long countBySiteId(SiteEntity siteId);

    Iterable<PageEntity> findBySiteId(SiteEntity sitePath);

    @Query(value = "select p.* from Page p join words_index i on p.id = i.page_id where i.lemma_id in :lemmas", nativeQuery = true)
    List<PageEntity> findByLemma(@Param("lemmas")List<LemmaEntity> lemma);

}
