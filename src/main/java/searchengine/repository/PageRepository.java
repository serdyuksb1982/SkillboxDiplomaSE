package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;


@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {
    Integer countBySiteId(SiteEntity site);

    PageEntity findByPath(String path);

}
