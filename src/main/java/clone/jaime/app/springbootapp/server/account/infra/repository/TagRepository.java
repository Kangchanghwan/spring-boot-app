package clone.jaime.app.springbootapp.server.account.infra.repository;

import clone.jaime.app.springbootapp.server.account.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTitle(String title);

}
