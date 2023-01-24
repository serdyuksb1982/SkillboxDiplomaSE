package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;


@Repository
public interface PageRepository extends JpaRepository<PageModel, Long> {
    long countBySiteId(SiteModel siteId);

    Iterable<PageModel> findBySiteId(SiteModel sitePath);

}
