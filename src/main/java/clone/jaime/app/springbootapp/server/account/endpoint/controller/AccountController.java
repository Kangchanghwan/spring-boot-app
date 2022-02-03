package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.validator.SignUpFormValidator;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    private final AccountService accountService;
    private final AccountRepository accountRepository;

    @InitBinder("signUpForm") // (1)
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }
  //validator 사용 방법은 위 방법도 가능하다.


    @GetMapping("/sign-up")
    public String signForm(Model model) {
        model.addAttribute(new SignUpForm());
        //attribute를 추가할때 클래스의 camel-case와 동일한 키를 사용하는 경우 키를 별도로 지정할
        //필요가 없다.
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(Model model,
            @Valid @ModelAttribute SignUpForm signUpForm,
            Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.signup(signUpForm);
        accountService.login(account);

        //회원 가입시 자동로그인이 되게 한다.
        return "redirect:/";
    }
    @GetMapping("/check-email-token")
    @Transactional
    public String verifyEmail(String token,String email, Model model){
        //이메일 링크를 클릭하면 접속하게 되는 컨트롤러이다.
        Account account = accountService.findAccountByEmail(email);
        //accountService에게 계정정보를 가져오도록 위임한다.
        if(account == null){
            model.addAttribute("error", "wrong.email");
            return "account/email-verification";
        }
        //가입된 사용자가 아닌경우 오류를 발생시킨다.
        if(!token.equals(account.getEmailToken())){
            model.addAttribute("error","wrong.token");
            return "account/email-verification";
        }
        //가입된 사용자이나 발급한 토큰과 맞지 않는다면 오류를 발생시킨다.
        account.verified();
        //인증 완료된 계정의 인증정보를 완료로 바꾼다.
        accountService.login(account);
        model.addAttribute("numberOfUser",accountRepository.count());
        model.addAttribute("nickname",account.getNickname());
        //인증에 성공할 시 보여줄 정보를 model에 담아 보낸다.
        return "account/email-verification";
    }
    @GetMapping("/check-email")
    public String checkMail(@CurrentUser Account account, Model model){
        model.addAttribute("email",account.getEmail());
        //model에 email을 넣어준더.,
        return "account/check-email";
    }
    @GetMapping("/resend-email")
    public String resendEmail(@CurrentUser Account account, Model model){
        if(account.enableToSendEmail()){
            //이메일 재전송할 때 호출되는 부분으로 새로고침이나 악용하지 못하도록 5분에
            //한 번만 메일을 보낼 수 있도록 방어 로직을 만든다.
            model.addAttribute("error","인증 이메일 5분에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email",account.getEmail());
            return "account/check-email";
        }
        accountService.saveVerificationEmail(account);
        return "redirect:/";
    }



}
