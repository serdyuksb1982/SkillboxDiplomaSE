package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {

    List<LemmaEntity> findBySiteEntityId(SiteEntity siteId);

    long countBySiteEntityId(SiteEntity site);

    @Query(value = "SELECT l.* from Lemma l where l.lemma = :lemma order by frequency", nativeQuery = true)
    List<LemmaEntity> findByLemma(@Param("lemma") String lemma);

}
