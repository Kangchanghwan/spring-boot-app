package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.mail.EmailService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.WithAccount;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import clone.jaime.app.springbootapp.server.study.application.StudyService;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.endpoint.form.StudyForm;
import clone.jaime.app.springbootapp.server.study.infra.repository.StudyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class StudySettingControllerTest {
    private static final String path = "study-path";
    @MockBean
    EmailService emailService;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    StudyService studyService;
    @Autowired
    StudyRepository studyRepository;
    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void beforeEach(){
        Account account = accountRepository.findByNickname("abc");
        studyService.createNewStudy(StudyForm.builder()
                .path(path)
                .shortDescription("short-description")
                .fullDescription("full-description")
                .title("title")
                .build(),account);
    }

    @AfterEach
    void afterEach() {
        studyRepository.deleteAll();
    }

    @Test
    @DisplayName("스터디 세팅 폼 조회")
    @WithAccount("abc")
    void studyForm () throws Exception{
        mockMvc.perform(get("/study/"+ path +"/settings/description"))
                .andExpect(status().isOk())
                .andExpect(view().name("study/settings/description"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @Test
    @DisplayName("스터디 세팅 수정: 정상")
    @WithAccount("abc")
    void createStudy () throws Exception{
        Account account =  accountRepository.findByNickname("abc");
        String shortDescriptionToBeUpdate = "short-description-test";
        String fullDescriptionToBeUpdate = "full-description-test";
        mockMvc.perform(post("/study/"+ path +"/settings/description")
                        .param("shortDescription", shortDescriptionToBeUpdate)
                        .param("fullDescription",fullDescriptionToBeUpdate)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + path + "/settings/description"));
        Study study = studyService.getStudy(account,path);
        assertEquals(shortDescriptionToBeUpdate, study.getShortDescription());
        assertEquals(fullDescriptionToBeUpdate, study.getFullDescription());
    }

}
