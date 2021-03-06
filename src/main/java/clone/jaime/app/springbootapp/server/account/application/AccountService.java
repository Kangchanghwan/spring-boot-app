package clone.jaime.app.springbootapp.server.account.application;


import clone.jaime.app.springbootapp.config.AppProperties;
import clone.jaime.app.springbootapp.mail.EmailMessage;
import clone.jaime.app.springbootapp.mail.EmailService;
import clone.jaime.app.springbootapp.server.account.domain.UserAccount;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.NotificationForm;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.Profile;
import clone.jaime.app.springbootapp.server.account.endpoint.controller.form.SignUpForm;
import clone.jaime.app.springbootapp.server.account.infra.repository.AccountRepository;
import clone.jaime.app.springbootapp.server.tag.domain.entity.Tag;
import clone.jaime.app.springbootapp.server.zone.domain.entity.Zone;
import lombok.RequiredArgsConstructor;
import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.json.simple.JSONObject;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {


    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    // 스프링 설정 정보를 가져오는 객체

    private final AccountRepository accountRepository;
    //계정 정보를 저장하기 위해 repository를 주입
    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;
    //비밀번호를 변경해주는 클래스를 주입 한다.

    /**
     * 이메일 인증을 위해 이메일로 토큰링크를 보내주는 기능이다.
     * @param newAccount
     */

    public void saveVerificationEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", String.format("/check-email-token?token=%s&email=%s",
                newAccount.getEmailToken(), newAccount.getEmail()));
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message","가입 인증을 위해 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        emailService.sendEmail(EmailMessage.builder()
                .to(newAccount.getEmail())
                .message(message)
                .subject("회원 가입 인증").build());
        //센더를 통해 메일을 보낸다.
    }

    /**
     * param으로 받은 form데이터를 account 객체에 빌드하여
     * save하는 기능이다.
     * @param signUpForm
     * @return
     */
    public Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.with(signUpForm.getEmail(), signUpForm.getNickname(), passwordEncoder.encode(signUpForm.getPassword()));
        account.generateToken();
        Account newAccount = accountRepository.save(account);
        return newAccount;
    }

    /**
     * 위 두개 매서드를 이용하여 가입과 인증을 동시 처리하는 가입메소드다.
     * @param signUpForm
     * @return
     */
    public Account signup(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        saveVerificationEmail(newAccount);
        return newAccount;
    }
    @Transactional(readOnly = true)
    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
        //이메일로 계정을 찾아 반환
    }

    /**
     * 회원가입시 자동 로그인을 담당하는 메소드다.
     * 우선 SecurityContextHolder.getContext()로 SecurityContext를 얻는다.
     * securityContextHolder : 인증정보를 관리하는 클래스이다.
     * 인증정보를 관리하는 Holder에서 context를 얻고 setAuthentication으로  인증정보를 넣어줄 수 있다.
     *
     * UsernamePasswordAuthenticationToken 로 토큰을 생성한다.
     * 생성시, nickname , password , Role 을 전달하는데,
     * Role은 인가 개념으로 권한 관련한 설정이다.
     *
     * 추가,
     * 인증정보를 더 전달하기 위해
     * nickname부분을 account 객체로 변경하였다.
     *
     * @param account
     */
    public void login(Account account){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(new UserAccount(account)
        ,account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        //singleTon은 List에 단 하나의 객체만 저징하기 위함임. 제네릭같은 기능인거같음.
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    /**
     *
     * 로그인 페이지 에서 로그인을 담당하는 메서드다
     * security 내부 로직에 의해 데이터베이스에서 ysername(email)을찾고
     * 비밀번호 매칭을 통해 로그인하는 로직이다.
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = Optional.ofNullable(accountRepository.findByEmail(username))
                .orElse(accountRepository.findByNickname(username));
        if(account == null){
            throw  new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    public void verified(Account account) {
        account.verified();
        login(account);
    }

    public void updateProfile(Account account, Profile profile) {
        account.updateProfile(profile);
        accountRepository.save(account);
    }

    public void certifiedPhoneNumber(String phone, String numStr) {

        String api_key = "NCSIAHSMU6KDBN8E";
        String api_secret = "DPOSA19MTFMBHPQB2GNA7UNCP4CVYVBG";
        Message coolsms = new Message(api_key, api_secret);

        // 4 params(to, from, type, text) are mandatory. must be filled
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", phone);    // 수신전화번호
        params.put("from", "01027242549");    // 발신전화번호. 테스트시에는 발신,수신 둘다 본인 번호로 하면 됨
        params.put("type", "SMS");
        params.put("text", "핫띵크 휴대폰인증 테스트 메시지 : 인증번호는" + "["+numStr+"]" + "입니다.");
        params.put("app_version", "test app 1.2"); // application name and version

        try {
            JSONObject obj = (JSONObject) coolsms.send(params);
            System.out.println(obj.toString());
        } catch (CoolsmsException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getCode());
        }
    }

    public void updatePassword(Account account, String newPassword) {
        account.updatePassword(passwordEncoder.encode((newPassword)));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, NotificationForm form){
        account.updateNotification(form);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.updateNickname(nickname);
        accountRepository.save(account);
        login(account);
        //인증 정보를 업데이트 하였어도 실제 반영하기 위해서는 다시로그인 메서드를 호출해주어 인증정보를 갱신한다.
    }

    public void sendLoginLink(Account account) {
        account.generateToken();

        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "로그인 하기");
        context.setVariable("message","로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host",appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        emailService.sendEmail(EmailMessage.builder()
                .to(account.getEmail())
                .message(message)
                .subject("[CokeBear] 로그인 링크")
                .build());
    }

    public Set<Tag> getTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void addTag(Account account, Tag tag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().add(tag));
    }

    public void removeTag(Account account, Tag tag) {
        accountRepository.findById(account.getId()).ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).orElseThrow().getZones().add(zone);
    }

    public void removeZone(Account account, Zone zone) {
        accountRepository.findById(account.getId()).orElseThrow().getZones().remove(zone);

    }

    public Account getAccountBy(String nickname) {
        return Optional.ofNullable(accountRepository.findByNickname(nickname)).orElseThrow(
                ()-> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));
    }
}
