package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;

import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaModel, Long> {

    @Transactional
    LemmaModel getById(long lemmaID);

    @Transactional
    long countBySiteModelId(SiteModel site);

    @Transactional
    List<LemmaModel> findBySiteModelId(SiteModel siteId);

    @Transactional
    @Query(value = "select * from Lemma where Lemma.lemma in :lemmas AND Lemma.site_id = :site", nativeQuery = true)
    List<LemmaModel> findLemmaListBySite(List<String> lemmas, SiteModel site);

}
