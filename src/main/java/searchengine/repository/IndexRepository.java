package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;

@Repository
public interface IndexRepository extends JpaRepository<IndexModel, Long> {
}
