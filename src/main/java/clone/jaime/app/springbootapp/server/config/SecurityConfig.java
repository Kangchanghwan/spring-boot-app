package clone.jaime.app.springbootapp.server.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","/login","/sign-up"
                ,"/check-email","/check-email-login","login-link").permitAll()
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll()
                .anyRequest().authenticated();
        /**
         * 루트 페이지 /, 로그인 페이지, 회원 가입 페이지,
         * 이메일 체크하는 페이지 등 인증이 없어도 접근할 수 있는 url을 모두 등록했습니다.
         *
         * profile의 경우 다른 사람의 것도 조회할 수 있어야 하므로
         * GET 메서드를 사용하고 /profile로 시작하는 모든 url 또한 인증 없이 접근할 수 있게 하였습니다.
         */
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                .antMatchers("/h2-console/**");
    }
}
