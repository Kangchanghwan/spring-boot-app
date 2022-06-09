package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class MainControllerTest {

    @Autowired
    MockMvc mockMvc;
    //가상,.
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;
    @MockBean
    JavaMailSender mailSender;

    @BeforeEach
    void beforeEach() {
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setEmail("lgodl1234@naver.com");
        signUpForm.setNickname("코코아");
        signUpForm.setPassword("qwer1234!");
        accountService.signup(signUpForm);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }


    @Test
    @DisplayName("이메일로 로그인 : 성공")
    void login_with_email() throws Exception {

        mockMvc.perform(post("/login")
                        .param("id", "lgodl1234@naver.com")
                        .param("pw", "qwer1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("코코아"));
    }

    @Test
    @DisplayName("닉네임으로 로그인 : 성공")
    void login_with_nickname() throws Exception {
        mockMvc.perform(post("/login")
                        .param("id", "코코아")
                        .param("pw", "qwer1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("코코아"));
        //닉네임 로그인시 성공하고 . 성공한 인증정보의 닉네임이 일치 해야함.
    }

    @Test
    @DisplayName("로그인 실패")
    void login_fail() throws Exception {
        mockMvc.perform(post("/login")
                        .param("id", "test")
                        .param("pw", "qwer1234!")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(SecurityMockMvcResultMatchers.unauthenticated());
        //로그인 실패시 error페이지로 리다이렉트 되고
        // 인증되지 않은 상태를 검증
    }
    @Test
    @DisplayName("로그아웃 : 성공")
    void logout() throws Exception{
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
        //로그아웃시 루트로 리다이렉트 되고, 인증되지 않은 상태를 검증
    }

}