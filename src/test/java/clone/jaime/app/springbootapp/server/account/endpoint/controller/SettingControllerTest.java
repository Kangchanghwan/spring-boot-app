package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.WithAccount;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @MockBean
    JavaMailSender mailSender;


    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("프로필 수정: 입력값 정상")
    @WithAccount("abc")
    void updateProfile() throws Exception{
        String bio = "한줄 소개";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));
        Account cock = accountRepository.findByNickname("abc");
        assertEquals(bio,cock.getProfile().getBio());
    }

    @Test
    @DisplayName("프로필 수정 : 입력값 에러")
    @WithAccount("abc")
    void updateProfileWithError() throws  Exception{
        String bio = "35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 35자 넘으면 에러 ";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                        .param("bio",bio)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
        Account cock = accountRepository.findByNickname("abc");
    }

    @Test
    @DisplayName("프로필 조회")
    @WithAccount("abc")
    void updateProfileForm() throws Exception {
        String bio = "한줄 소개";
        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }


}