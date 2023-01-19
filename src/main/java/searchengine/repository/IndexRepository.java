package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    IndexEntity findByLemmaIdAndPageId(long lemmaId, long pageId);

    List<IndexEntity> findByPageId(long pageId);

    @Query(value = "select p.* from words_index p where p.lemma_id in :lemmas and p.page_id in :pages", nativeQuery = true)
    List<IndexEntity> findByPageAndLemma(@Param("lemmas")List<LemmaEntity> lemmaEntityListId,
                                         @Param("pages")List<PageEntity> pageEntityListId);

}
