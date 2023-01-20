package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

@Repository
public interface SiteRepository extends JpaRepository<SiteModel, Long> {
    SiteModel findByUrl(String url);

    SiteModel findByUrl(SiteModel site);
    SiteModel findByUrl(long id);

}
