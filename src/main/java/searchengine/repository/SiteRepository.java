package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.dto.statistics.model.SiteModel;

@Repository
public interface SiteRepository extends JpaRepository<SiteModel, Integer> {

    SiteModel findSiteByUrl(String url);
}
