package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.validator.SignUpFormValidator;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;
    //계정 정보를 저장하기 위해 repository를 주입
    private final JavaMailSender javamailSender;
    //인증메일을 보내기위해 JavaMailSender를 주입한다. 이때 실체로 메일을 보내는 것이아니라
    // 로컬에서 로그로만 확인하기위해 직접 구현해 주입할 수 있도록 바로 아래 설정할 예정합니다.

//    @InitBinder("signUpForm") // (1)
//    public void initBinder(WebDataBinder webDataBinder) {
//        webDataBinder.addValidators(signUpFormValidator);
//    }
//  validator 사용 방법은 위 방법도 가능하다.


    @GetMapping("/sign-up")
    public String signForm(Model model) {
        model.addAttribute(new SignUpForm());
        //attribute를 추가할때 클래스의 camel-case와 동일한 키를 사용하는 경우 키를 별도로 지정할
        //필요가 없다.
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(
            @Valid @ModelAttribute SignUpForm signUpForm,
            Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        signUpFormValidator.validate(signUpForm, errors);
        //작성한 Validator를 이용해 객체를 검증하고, 에러가 있을경우 기존과 동일하게 처리합니다.
        if (errors.hasErrors()) {
            return "account/sign-up";
        } // 이프문을 더 작성하는 이유는 유효성 검사하며 계속 데이터베이스 커넥트를 마르게 하지 않기 위함.
        Account account = Account.builder() // Entity 생성한다.
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(signUpForm.getPassword())
                //password의 경우 암호화가 필요하나 일단 평문으로 작성한다.
                .notificationSetting(Account.NotificationSetting.builder()
                                            .studyCreatedByWeb(true)
                                            .studyUpdatedByWeb(true)
                                            .studyRegistrationResultByEmailByWeb(true)
                                            .build())
                .build();
        Account newAccount = accountRepository.save(account);
        //레포지토리를 통해 저장한다.
        newAccount.generateToken();
        //이메일 인증용 토큰을 생성한다.
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        //이메일 객체를 생성하고 이메일의 내용을 채운다.
        mailMessage.setTo(newAccount.getEmail());
        mailMessage.setSubject("회원 가입 인증");
        mailMessage.setText(String.format("/check-email-token?token=%s&email=%s",
                newAccount.getEmailToken(),newAccount.getEmail()));
        //이메일 본문에 추가할 링크를 작성한다. 나중에 사용자가 링크를 클릭했을 때
        // 다시 서버로 요청해야하고 이 부분에 대한 구현이 되어있어야 이메일 인증을 마칠 수 있습니다.
        javamailSender.send(mailMessage);
        //센더를 통해 메일을 보낸다.
        return "redirect:/";
    }
    //기존에 내가 알고있던 BindingResult는 Errors를 구현한 클래스이므로
    // errors 인터페이스로 받아서 사용하는게 더 객체지향적이다.


}
