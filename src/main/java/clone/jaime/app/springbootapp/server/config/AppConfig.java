package clone.jaime.app.springbootapp.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /**
     * 로그에 잘 나와있지만 AccountService와 SecurityConfig가 서로 순환참조하고있는데요,
     * 그 이유는 AccountService에서 PasswordEncoder를 주입받아서 사용하는데 PasswordEncoder를 주입받기 위해서는 SecurityConfig 빈이 먼저 생성되어야 합니다.
     *
     * 하지만 SecurityConfig를 생성하려면 AccountService를 주입받아야하므로 순환 참조가 발생하는 것입니다.
     *
     * 따라서 이 원인이 되는 PasswordEncoder 빈을 다른 설정으로 옮겨줍니다.
     *
     * AppConfig 클래스를 생성해 PasswordEncoder 빈을 추가합니다.
     * @return
     */

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    //패스워드를 인코딩 해주는 메서드를 다른 곳에서도 사용할 수 있게 빈으로 등록해준다.


}
