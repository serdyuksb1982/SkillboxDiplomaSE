package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.dto.statistics.model.PageModel;
import searchengine.dto.statistics.model.SiteModel;


import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<PageModel, Integer> {

    List<PageModel> findAllBySite(SiteModel site);

    Integer countBySite(SiteModel site);

    PageModel findByPath(String path);
}
