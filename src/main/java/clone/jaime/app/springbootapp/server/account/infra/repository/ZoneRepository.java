package clone.jaime.app.springbootapp.server.account.infra.repository;

import clone.jaime.app.springbootapp.server.account.domain.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Optional<Zone> findByCityAndProvince(String cityName, String provinceName);
}
