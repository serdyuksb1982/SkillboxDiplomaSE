package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaModel, Long> {

    long countBySiteModelId(SiteModel site);

    List<LemmaModel> findBySiteModelId(SiteModel siteId);
    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma IN :lemmas AND l.site_id = :site", nativeQuery = true)
    List<LemmaModel> findLemmaListBySite(@Param("lemmas") List<String> lemmaList,
                                         @Param("site") SiteModel site);

    @Query(value = "SELECT l.* FROM Lemma l WHERE l.lemma = :lemma ORDER BY frequency ASC", nativeQuery = true)
    List<LemmaModel> findByLemma(@Param("lemma") String lemma);


}
