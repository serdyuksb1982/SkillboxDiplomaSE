package searchengine.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.SiteConfiguration;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {


    Integer countBySite(Site site);

    Page findByPath(String path);

}
