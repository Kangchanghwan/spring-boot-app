package clone.jaime.app.springbootapp.server.event.infra.repository;

import clone.jaime.app.springbootapp.server.event.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event,Long> {
}
