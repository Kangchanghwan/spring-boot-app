package clone.jaime.app.springbootapp.server.study.infra.repository;

import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study,Long> {

    boolean existsByPath (String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);
    @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)

    Study findStudyWithTagsByPath(String path);
    @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)

    Study findStudyWithZonesByPath(String path);
    @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)

    Study findStudyWithManagersByPath(String path);
    @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)

    Study findStudyWithMembersByPath(String path);
    //Path가 존재하는지 확인하는 메서드

    Optional<Study> findStudyByPath (String path);

}
