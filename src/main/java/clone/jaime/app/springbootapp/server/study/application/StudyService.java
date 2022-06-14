package clone.jaime.app.springbootapp.server.study.application;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyDescriptionForm;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyForm;
import clone.jaime.app.springbootapp.server.study.infra.repository.StudyRepository;
import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.zone.domain.entity.Zone;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;


    public Study getStudyToUpdateStatus(Account account, String path){
        return  getStudy(account,path,studyRepository.findStudyWithManagersByPath(path));
    }

    public void publish(Study study){
        study.published();
    }
    public void close(Study study){
        study.close();
    }
    public void startRecruit(Study study){
        study.startRecruit();
    }
    public void stopRecruit(Study study){
        study.stopRecruit();
    }
    public boolean isValidPath(String newPath){
        if(!newPath.matches(StudyForm.VALID_PATH_PATTERN)){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }
    public void updateStudyPath(Study study, String newPath){
        study.updatePath(newPath);
    }
    public boolean isValidTitle(String newTitle){
        return newTitle.length() <= 50;
    }
    public void updateStudyTitle(Study study,String newTitle){
        study.updateTitle(newTitle);
    }
    public void remove(Study study){
        if (!study.isRemovable()) {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
        studyRepository.delete(study);
    }

    public Study createNewStudy( StudyForm studyForm, Account account){
        Study study = Study.from(studyForm);
        study.addManager(account);
        return studyRepository.save(study);
    }

    public Study getStudy(Account account, String path) {
        Study study = studyRepository.findByPath(path);
        checkStudyExists(path,study);
        return study;
    }

    public Study getStudy(String path){
        Study study = studyRepository.findByPath(path);
        if(study == null){
            throw  new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
        return study;
    }

    public void updateStudyDescription(Study study , StudyDescriptionForm studyDescriptionForm){
        study.updateDescription(studyDescriptionForm);
    }

    public void updateStudyImage(Study study, String image) {
        study.updateImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setBanner(false);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkStudyExists(path,study);
        checkAccountIsManager(account,study);
        return study;
    }
    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkStudyExists(path,study);
        checkAccountIsManager(account,study);
        return study;
    }

    private void checkAccountIsManager(Account account, Study study) {
        if(!account.isManagerOf(study)){
            throw new IllegalArgumentException("해당 기능을 사용할 수 없습니다.");
        }
    }

    private void checkStudyExists(String path, Study study) {
        if(study == null){
            throw new IllegalArgumentException(path+"에 해당하는 스터디가 없습니다.");
        }
    }

    public void addTag(Study study, Tag tag) {
        study.addTag(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.removeTag(tag);
    }


    public void addZone(Study study, Zone zone) {
        study.addZone(zone);
    }

    public void removeZone(Study study, Zone zone) {
        study.removeZone(zone);
    }

    public Study getStudyToUpdate(Account account, String path){
        return getStudy(account,path,studyRepository.findByPath(path));
    }

    private Study getStudy(Account account, String path, Study studyByPath) {
        checkStudyExists(path,studyByPath);
        checkAccountIsManager(account,studyByPath);
        return studyByPath;
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }
    public void removeMember(Study study,Account account){
        study.removeMember(account);
    }
}
