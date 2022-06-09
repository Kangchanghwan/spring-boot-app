package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.SignUpForm;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.validator.SignUpFormValidator;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Random;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }
    // 뷰 페이지로 라우팅

    @PostMapping("/email-login")
    public String sendLinkForEmailLogin(String email, Model model, RedirectAttributes attributes) {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if (!account.enableToSendEmail()) {
            model.addAttribute("error", "너무 잦은 요청입니다. 5분 뒤에 다시 시도하세요.");
            return "account/email-login";
        }
        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "로그인 가능한 링크를 이메일로 전송하였습니다.");
        return "redirect:/email-login";
    }
    // 이메일 폼을 통해 입력받은 이메일로 이메일을 발송한다. 이메일이 올바르지 않을경우 에러를 전달한다.

    @GetMapping("login-by-email")
    public String loginByEmail(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isValid(token)) {
            model.addAttribute("error", "로그인할 수 없습니다.");
            return "account/logged-in-by-email";
        }
        accountService.login(account);
        return "account/logged-in-by-email";
    }
    // 토큰의 유효성을 판단한다. 유효한 토큰인경우 로그인 시킨다.

    @InitBinder("signUpForm") // (1)
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }
    //validator 사용 방법은 위 방법도 가능하다.

    @PostMapping("/sign-up/sendSMS")
    public @ResponseBody String sendSMS(@RequestBody SignUpForm phone) {
        System.out.println("phone = " + phone.getPhone());

        Random rand  = new Random();
        String numStr = "";
        for(int i=0; i<4; i++) {
            String ran = Integer.toString(rand.nextInt(10));
            numStr+=ran;
        }

        System.out.println("수신자 번호 : " + phone);
        System.out.println("인증번호 : " + numStr);
       // accountService.certifiedPhoneNumber(phone.getPhone(),numStr);
        return numStr;
    }

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
        accountService.verified(account);
        //인증 완료된 계정의 인증정보를 완료로 바꾼다.
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
        if (!account.enableToSendEmail()) {
            //이메일 재전송할 때 호출되는 부분으로 새로고침이나 악용하지 못하도록 5분에
            //한 번만 메일을 보낼 수 있도록 방어 로직을 만든다.
            model.addAttribute("error", "인증 이메일 5분에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.saveVerificationEmail(account);
        return "redirect:/";
    }
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname,
                              Model model,
                              @CurrentUser Account account){
        Account byNickname = accountRepository.findByNickname(nickname);
        //파라미터로 가져온 닉네임으로 account를 찾는다.
        if(byNickname == null){
            //만일 없다면
            throw new IllegalArgumentException(nickname+"에 해당하는 사용자가 없습니다.");
            //오류발생
        }
        log.info("byNickname = {}",byNickname);
        model.addAttribute(byNickname);
        //있다면 객체를 모델에 담아 보낸다.
        model.addAttribute("isOwner",byNickname.equals(account));
        //해당 프로필이 내 프로필인지 아닌지 확인한 값을 모델객체에 같이 담아 보낸다.
        return "account/profile";
    }

}
