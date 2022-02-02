package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.validator.SignUpFormValidator;
import lombok.RequiredArgsConstructor;
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
    private final AccountService accountService;

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
    public String signUpSubmit(
            @Valid @ModelAttribute SignUpForm signUpForm,
            Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        accountService.signup(signUpForm);
        return "redirect:/";
    }




}
