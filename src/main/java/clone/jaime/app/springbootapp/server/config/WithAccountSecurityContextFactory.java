package clone.jaime.app.springbootapp.server.config;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.WithAccount;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.SignUpForm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithAccountSecurityContextFactory implements WithSecurityContextFactory<WithAccount> {

    private final AccountService accountService;

    public WithAccountSecurityContextFactory(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public SecurityContext createSecurityContext(WithAccount annotation) {
        String nickname = annotation.value();

        SignUpForm signUpForm = new SignUpForm();

        signUpForm.setNickname(nickname);
        signUpForm.setEmail(nickname+"@gmail.com");
        signUpForm.setPassword("1234asdf");
        accountService.signup(signUpForm);

        UserDetails principal = accountService.loadUserByUsername(nickname);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal,principal.getPassword(),principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        return context;
    }
}
