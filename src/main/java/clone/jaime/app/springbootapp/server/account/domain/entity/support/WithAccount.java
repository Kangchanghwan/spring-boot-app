package clone.jaime.app.springbootapp.server.account.domain.entity.support;

import clone.jaime.app.springbootapp.server.config.WithAccountSecurityContextFactory;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAccountSecurityContextFactory.class)
//런타임시 동작하도록 설정
public @interface WithAccount {
    String value();
}
