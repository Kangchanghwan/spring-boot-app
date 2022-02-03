package clone.jaime.app.springbootapp.server.account.domain.entity.support;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
//런타임시 유지 되어야 한다.
@Target(ElementType.PARAMETER)
//파라미터에 사용할 수 있어야 한다.
@AuthenticationPrincipal(expression = "#this=='anonymousUser' ? null : account")
//spEL 을 이용하여 인증정보가 존재하지 않음면 null을, 존재하면 account 라는 property를 반환한다.
//기존 어노테이션은 값이 없으먄 anonymousUser를 반환한다.
public @interface CurrentUser {
}
