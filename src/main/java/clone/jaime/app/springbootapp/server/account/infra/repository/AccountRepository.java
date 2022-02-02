package clone.jaime.app.springbootapp.server.account.infra.repository;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
//성능의 이점을 가져오기위해 readonly 옵션을 true로 지정합니다.
public interface AccountRepository extends JpaRepository<Account,Long> {
    boolean existsByEmail(String email);
    //이메일 중복여부를 확인합니다.
    boolean existsByNickname(String nickname);
    //닉네임 중복여부를 확인합니다.
    Account findByEmail(String email);
}
