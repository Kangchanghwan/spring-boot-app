package clone.jaime.app.springbootapp.server.account.application;


import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.SignUpForm;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    //계정 정보를 저장하기 위해 repository를 주입
    private final JavaMailSender javamailSender;
    //인증메일을 보내기위해 JavaMailSender를 주입한다. 이때 실체로 메일을 보내는 것이아니라
    // 로컬에서 로그로만 확인하기위해 직접 구현해 주입할 수 있도록 바로 아래 설정할 예정합니다.
    private final PasswordEncoder passwordEncoder;
    //비밀번호를 변경해주는 클래스를 주입 한다.
    public void saveVerificationEmail(Account newAccount) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        //이메일 객체를 생성하고 이메일의 내용을 채운다.
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원 가입 인증");
        mailMessage.setText(String.format("/check-email-token?token=%s&email=%s",
                newAccount.getEmailToken(), newAccount.getEmail()));
        //이메일 본문에 추가할 링크를 작성한다. 나중에 사용자가 링크를 클릭했을 때
        // 다시 서버로 요청해야하고 이 부분에 대한 구현이 되어있어야 이메일 인증을 마칠 수 있습니다.
        javamailSender.send(mailMessage);
        //센더를 통해 메일을 보낸다.
    }

    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder() // Entity 생성한다.
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                //password의 경우 암호화가 필요하나 일단 평문으로 작성한다.
                .notificationSetting(Account.NotificationSetting.builder()
                        .studyCreatedByWeb(true)
                        .studyUpdatedByWeb(true)
                        .studyRegistrationResultByEmailByWeb(true)
                        .build())
                .build();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }
    public void signup(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        newAccount.generateToken();
        //이메일 인증용 토큰을 생성한다.
        saveVerificationEmail(newAccount);
    }

}
