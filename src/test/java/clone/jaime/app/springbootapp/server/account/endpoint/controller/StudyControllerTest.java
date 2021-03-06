package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.mail.EmailService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.WithAccount;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.Profile;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import clone.jaime.app.springbootapp.server.study.application.StudyService;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyForm;
import clone.jaime.app.springbootapp.server.study.infra.repository.StudyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    StudyService studyService;
    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("스터디 가입")
    @WithAccount(value = {"coke","test"})
    void joinStudy() throws Exception{

        Account manager = accountRepository.findByNickname("coke");
        String studyPath = "study-path";
        Study study = studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("studyTitle")
                .fullDescription("full~")
                .shortDescription("short~").build(),manager );
        mockMvc.perform(get("/study/" + studyPath + "/join"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath + "/members"));
        Account member = accountRepository.findByNickname("test");
        assertTrue(study.getMembers().contains(member));
    }

    @Test
    @DisplayName("스터디 탈퇴")
    @WithAccount(value = {"coke","test"})
    void leaveStudy() throws Exception{

        Account manager = accountRepository.findByNickname("coke");
        String studyPath = "study-path";
        Study study = studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("studyTitle")
                .fullDescription("full~")
                .shortDescription("short~").build(),manager );
        Account member = accountRepository.findByNickname("test");

        study.addMember(member);
        mockMvc.perform(get("/study/" + studyPath + "/leave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath + "/members"));
        assertFalse(study.getMembers().contains(member));
    }

    @Test
    @DisplayName("스터디 멤버 뷰")
    @WithAccount("abc")
    void studyMemberView() throws Exception{
        Account account = accountRepository.findByNickname("abc");
        account.updateProfile(Profile.from(account));
        String studyPath = "study-path";
        studyService.createNewStudy(StudyForm.builder()
                .path(studyPath)
                .title("study-title")
                .shortDescription("short-description")
                .fullDescription("full-description")
                .build(),account);
        mockMvc.perform(get("/study/" + studyPath + "/members"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/members"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }


    @Test
    @DisplayName("스터디 뷰")
    @WithAccount("abc")
    void studyView() throws Exception{
        Account account = accountRepository.findByNickname("abc");
        String studyPath = "study-path";
        studyService.createNewStudy(StudyForm.builder()
                        .path(studyPath)
                        .title("study-title")
                        .shortDescription("short-description")
                        .fullDescription("full-description")
                .build(),account);
        mockMvc.perform(get("/study/" + studyPath))
                .andExpect(status().isOk())
                .andExpect(view().name("study/view"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("study"));
    }


    @Test
    @DisplayName("스터디 폼 조회")
    @WithAccount("abc")
    void studyForm() throws Exception{
        mockMvc.perform(get("/new-study"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyForm"));
    }
    @Test
    @DisplayName("스터디 추가 : 입력값 정상")
    @WithAccount("abc")
    void createStudy() throws Exception{
        String studyPath = "study-path";
        mockMvc.perform(post("/new-study")
                .param("path",studyPath)
                .param("title","어서오세요")
                .param("shortDescription","여기는 아마존 입니다.")
                .param("fullDescription","아마존조로존존존 입니다 다 젖어요")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + studyPath));
        assertTrue(studyRepository.existsByPath(studyPath));
    }

    @Test
    @DisplayName("스터디 추가 : 입력값 비정상")
    @WithAccount("abc")
    void createStudyWithError() throws Exception{
        String studyPath = "s";
        mockMvc.perform(post("/new-study")
                        .param("path",studyPath)
                        .param("title","어서오세요")
                        .param("shortDescription","여기는 아마존 입니다.")
                        .param("fullDescription","아마존조로존존존 입니다 다 젖어요")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors());
    }


    @Test
    @DisplayName("스터디 추가 : 입력값 중복")
    @WithAccount("abc")
    void createStudyWithDuplicate() throws Exception{

        Account account = accountRepository.findByNickname("abc");
        String duplicatePath = "study-path";
        studyService.createNewStudy(
                StudyForm.builder()
                        .fullDescription("fullDescription")
                        .shortDescription("shortDescription")
                        .title("title")
                        .path(duplicatePath)
                        .build(), account);

        mockMvc.perform(post("/new-study")
                        .param("path",duplicatePath)
                        .param("title","어서오세요")
                        .param("shortDescription","여기는 아마존 입니다.")
                        .param("fullDescription","아마존조로존존존 입니다 다 젖어요")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("study/form"))
                .andExpect(model().hasErrors());
    }




}
