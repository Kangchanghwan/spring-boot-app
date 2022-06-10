package clone.jaime.app.springbootapp.server.account.application;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.study.Study;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.StudyForm;
import clone.jaime.app.springbootapp.server.account.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;

    public Study createNewStudy( StudyForm studyForm, Account account){
        Study study = Study.from(studyForm);
        study.addManager(account);
        return studyRepository.save(study);
    }

}
