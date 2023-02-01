package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String userName);
}