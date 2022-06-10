package clone.jaime.app.springbootapp.server.account.infra.repository;

import clone.jaime.app.springbootapp.server.account.domain.entity.study.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudyRepository extends JpaRepository<Study,Long> {

    boolean existsByPath (String path);
    //Path가 존재하는지 확인하는 메서드


}
