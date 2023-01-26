package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
@Transactional
public interface SiteRepository extends JpaRepository<SiteModel, Long> {
    SiteModel findByUrl(String url);

}
