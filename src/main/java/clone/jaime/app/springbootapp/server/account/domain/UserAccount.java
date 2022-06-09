package clone.jaime.app.springbootapp.server.account.domain;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * principle에선 account가 아닌 nickname이 들어있다.
 * 따라서 객체로 반환하고 싶다면 UserDetailsService를 구현해야 한다.
 * 허나 security 패키지의 user가 해당 인터페이스를 구현했기 때문에
 * user를 상속받으면 된다.
 */
public class UserAccount extends User {

    @Getter
    private final Account account;
    //@CurrentUser 어노테이션에서 account를 반환하도록 하였기 때문에
    // 반드시 변수이름을 account로 동일시 해야한다.

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
    //User객체를 생성하기 위해선 username, password, authorities 가 필요한데
    //우리가 사용화는 객체인 Account에서 각각 추출해 준다.


}
