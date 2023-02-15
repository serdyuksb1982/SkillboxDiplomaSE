package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteModel;



@Repository
public interface SiteRepository extends JpaRepository<SiteModel, Long> {

    @Transactional
    SiteModel findByUrl(String url);
}
