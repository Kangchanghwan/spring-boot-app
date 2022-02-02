package clone.jaime.app.springbootapp.server.account.endpoint.controller.validator;

import clone.jaime.app.springbootapp.server.account.endpoint.controller.SignUpForm;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
//빈주입을 위해 component 스캔이 되게 한다.
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {
    //validator 인터페이스를 구현하여 커스텀 한다.

    private final AccountRepository accountRepository;
    // RequiredArgsConstructor로 주입한다.

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }
    //SignUpForm클래스 일때만 검증을 수행하기 위해 지원하는 타입을 지정한다.
    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid.email",
                    new Object[]{signUpForm.getEmail()},
                    "이미 사용중인 이메일 입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid.nickname",
                    new Object[]{signUpForm.getNickname()},
                    "이미 사용중인 닉네임 입니다.");
        }

    }
}
