package clone.jaime.app.springbootapp.server.account.endpoint.controller.validator;

import clone.jaime.app.springbootapp.server.account.endpoint.controller.PasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class PasswordFormValidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword","wrong.value","입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
