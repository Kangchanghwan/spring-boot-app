package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.mail.EmailMessage;
import clone.jaime.app.springbootapp.mail.EmailService;
import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    //이메일 검증을 위해 repository를 주입해준다.

    //메일을 전송했는지 확인한다.
    //실제로 전송야부를 확인하긴 어렵기 때문에 mockBean을 이용하여 주입한다.
    //mailSender가 send라는 메서드를 호출하고 그때 전달된 타입이 Simplemassage 타입인지 확인한다.\

    @MockBean
    EmailService emailService;
    @Autowired
    AccountService accountService;
    /**
     * 회원 가입시 이상한 값이 입력된 경우
     * 다시 회원 가입 화면으로 리다이렉트 하는지 확인
     * 에러가 잘 노출 되는지 확인
     * 회원 가입시 정상적인 값이 입력된 경우
     * 가입한 회원 데이터가 존재하는지 확인
     * 이메일이 보내지는지 확인
     *
     * @throws Exception
     */

    @Test
    @DisplayName("회원 가입 화면 진입 확인")
    void signUpForm() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                //Http status가 200 Ok 인지 확인합니다.
                .andExpect(view().name("account/sign-up"))
                //view가 제대로 이동했는지 확인합니다.
                .andExpect(model().attributeExists("signUpForm"))
                //객체로 전달했던 attribute가 존재하는지 확인합니다.
                .andExpect(unauthenticated());
                 //인증 실패인지 확인한다.
    }

    @Test
    @DisplayName("회원 가입 처리 : 입력 값 오류")
    void signUpSubmitWithError() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "킹받네")
                        .param("email", "lgodl1596@naver")
                        //이메일을 일부러 포멧에 맞지 않게 설정하였다.
                        .param("password", "1234!")
                        //비밀번호도 일부러 8자리가 안되게 설정하였다.
                        .with(csrf()))
                //spring Security가 설정되어 있기때문에 csrf를 추가 해준다.
                //토큰이 없는경우 403 권한 없음 에러가 발생한다.
                .andDo(print())
                .andExpect(status().isOk())
                // 처리여부와 상관없으 200을 반환한다.
                .andExpect(view().name("account/sign-up"));
        //입력 값이 잘못되었기 때문에 /sign-up페이지로 돌아간다.
    }

    @Test
    @DisplayName("회원 가입 처리 : 입력 값 정상")
    void signUpSubmit() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "킹받네")
                        .param("email", "lgodl1596@naver.com")
                        .param("password", "1q2w3e4r!")
                        .param("phone","01027242549")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                //모두 정상일 경우 3xx redirect 상태코드를 발생시키는지 확인한다.
                .andExpect(view().name("redirect:/"))
                //루트페이지로 이동했는지 확인한다.
                .andExpect(authenticated().withUsername("킹받네"));
                //인증정보의 닉네임을 확인한다.
        assertTrue(accountRepository.existsByEmail("lgodl1596@naver.com"));
        //이메일이 정상적으로 저장되었는지 확인한다.
        Account account = accountRepository.findByEmail("lgodl1596@naver.com");
        assertNotEquals(account.getPassword(), "1q2w3e4r!");
        then(emailService)
                .should()
                .sendEmail(any(EmailMessage.class));
        //메일을 전송했는지 확인 후 메일 타입이 SimpleMailMessage타입인지 확인한다.
    }

    @Test
    @DisplayName("인증 메일 확인 : 잘못된 링크")
    void verifyEmailWithWrongLink() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "1234")
                        //유효하지 않는 토큰 값을 보낸다.
                        .param("email", "lgodl1598@naver.com"))
                .andDo(print())
                .andExpect(status().isOk())
                //상태값은 202여야 하나
                .andExpect(view().name("account/email-verification"))
                .andExpect(model().attributeExists("error"))
                 //model에 error를 키로 갖는 객체가 있어야한다.
                .andExpect(unauthenticated());
                //인증이 실패했는지 확인한다.
    }

    @Test
    @DisplayName("인증 메일 확인 : 유효한 링크")
    @Transactional
    void verifyEmail() throws Exception {
        Account account = Account.builder()
                .email("email@email.com")
                .password("1q2w3e4r!")
                .nickname("코카곰")
                .notificationSetting(Account.NotificationSetting.builder()
                        .studyCreatedByWeb(true)
                        .studyUpdatedByWeb(true)
                        .studyRegistrationResultByEmailByWeb(true)
                        .build())
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateToken();
        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/email-verification"))
                .andExpect(model().attributeExists("numberOfUser", "nickname"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(authenticated().withUsername("코카곰"));
                // 유효한 링크일 경우 인증 정보 확인.
    }


}