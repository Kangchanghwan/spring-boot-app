package clone.jaime.app.springbootapp.server.account.endpoint.controller;


import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class SettingController {

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME;
    // 테스트 코드에서 재사용 하기 위해 default로 접근제한자 설정을 해준다.
    private final AccountService accountService;

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdate(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(Profile.from(account));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account
            , @Valid Profile profile, Errors errors, Model model, RedirectAttributes attributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        attributes.addFlashAttribute("message","프로필을 수정하였습니다.");
        accountService.updateProfile(account,profile);
        return "redirect:" + SETTINGS_PROFILE_URL;
    }


}
