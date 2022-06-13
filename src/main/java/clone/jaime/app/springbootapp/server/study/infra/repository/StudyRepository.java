package clone.jaime.app.springbootapp.server.study.infra.repository;

import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long> {

    boolean existsByPath (String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
    //Path가 존재하는지 확인하는 메서드


}
