package clone.jaime.app.springbootapp.server.config;

import clone.jaime.app.springbootapp.server.account.application.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    //userDetailsService 에 설정하기 위해 주입
    private final DataSource dataSource;
    //토큰 저장소를 설정하기 위해 주입한다.

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up","/sign-up/*"
                ,"/check-email-token","/check-email-login","/email-login","login-link").permitAll()
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                .anyRequest().authenticated();
        http.formLogin()
                .loginPage("/login")
                .usernameParameter("id")
                .passwordParameter("pw")
                .permitAll();
        //formLogin을 설정하면 form기반 인증을 지원합니다.
        //2번의 loginpage를 설정하지 않으면 스프링이 기본으로 로그인페이지를 생성해 준다.
        http.logout()
                .logoutSuccessUrl("/");
        //로그아웃시 설정 지원
        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());
        /**
         * 루트 페이지 /, 로그인 페이지, 회원 가입 페이지,
         * 이메일 체크하는 페이지 등 인증이 없어도 접근할 수 있는 url을 모두 등록했습니다.
         *
         * profile의 경우 다른 사람의 것도 조회할 수 있어야 하므로
         * GET 메서드를 사용하고 /profile로 시작하는 모든 url 또한 인증 없이 접근할 수 있게 하였습니다.
         */
        http.csrf().disable();
    }

    @Bean
    //토큰 관리를 위한 Repository 구현체를 추가하는데 직접 구현할 필요가 없고,
    //dataSource만 설정해 주면 된다.
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .antMatchers("/h2-console/**");
    }

}
