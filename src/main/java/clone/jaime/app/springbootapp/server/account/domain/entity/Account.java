package clone.jaime.app.springbootapp.server.account.domain.entity;


import clone.jaime.app.springbootapp.server.account.endpoint.controller.NotificationForm;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
//직력화를 위해 생성자가 필요하나 외부 접근을 막기위해 엑세스 레벨을 Protected로 설정한다.
@Builder
//builder역시 생성자가 필요하다.
@Getter
@ToString
// 값조회만 가능하게 하였고, 값 세팅은 필요에따라 메서드로 추가할 예정
public class Account extends AuditingEntity{

    @Id @GeneratedValue
    @Column(name="account_id")
    private Long id;//기본키 id 인덱스 자동증가
    @Column(unique = true)
    private String email;
    //id가 될 이메일이다. 유니크 제약 조건을 추가해 고유의 값만 추가할 수 있다.
    @Column(unique = true)
    private String nickname;
    //사용자들에게 노출될 닉네임이다. 유니크 제약 조건을 추가해 고유값만 가질 수 있다.
    private String password;
    private LocalDateTime joinedAt;
    private Boolean isValid;
    private LocalDateTime emailTokenGeneratedAt;
    private String phone;
    private String emailToken;

    @Embedded
    private Profile profile;

    @Embedded
    private NotificationSetting notificationSetting;


    public boolean isValid(String token) {
        return this.emailToken.equals(token);
    }


    public void generateToken() {
        this.emailToken = UUID.randomUUID().toString();
        //UUID(Universally Unique IDentifier)
        //네트워크 상에서 고유성이 보장되는 id를 만들기 위한 표준 규약.
        this.emailTokenGeneratedAt = LocalDateTime.now();
        //메일을 보낼때 같이 시간을 지정.
    }

    public void verified() {
        this.isValid = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean enableToSendEmail() {
        return this.emailTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(5));
        //5분이 지났는지 체크한다.
    }


    @PostLoad
    // @Embedded 를 사용했을 떄 자동으로 초기화 되지 않아 템프릿 로드시 에러가 발생하여 , Entity로드 이후 null일 경우 자동 객체 생성
    private void init(){
        if(profile==null){
            profile = new Profile();
        }
        if(notificationSetting == null){
            notificationSetting = new NotificationSetting();
        }
    }

    public void updateProfile(clone.jaime.app.springbootapp.server.account.endpoint.controller.Profile profile) {
        if(this.profile == null){
            this.profile = new Profile();
        }

        this.profile.bio = profile.getBio();
        this.profile.url = profile.getUrl();
        this.profile.job = profile.getJob();
        this.profile.location = profile.getLocation();
        this.profile.image = profile.getImage();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }


    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter
    @ToString
    public static class Profile {
        private String bio;
        private String url;
        private String job;
        private String location;
        private String company;
        @Lob @Basic(fetch = FetchType.EAGER)
        private String image;
    }

    public void updateNotification(NotificationForm form) {
        this.notificationSetting.studyCreatedByEmail = form.isStudyCreatedByEmail();
        this.notificationSetting.studyCreatedByWeb = form.isStudyCreatedByWeb();
        this.notificationSetting.studyRegistrationResultByEmailByEmail = form.isStudyRegistrationResultByEmail();
        this.notificationSetting.studyRegistrationResultByEmailByWeb = form.isStudyRegistrationResultByWeb();
        this.notificationSetting.studyUpdatedByEmail = form.isStudyUpdatedByEmail();
        this.notificationSetting.studyUpdatedByWeb = form.isStudyUpdatedByWeb();
    }


    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter
    @ToString
    public static class NotificationSetting{
        private Boolean studyCreatedByEmail = false;
        private Boolean studyCreatedByWeb = true;
        private Boolean studyRegistrationResultByEmailByEmail = false;
        private Boolean studyRegistrationResultByEmailByWeb = true;
        private Boolean studyUpdatedByEmail = false;
        private Boolean studyUpdatedByWeb = true;

    }
    //equals는 기본적으로 인스턴스가 동일한 경우만 true를 반환한다.
    //JPA의 경우 1차캐시에서 생성한 엔티티와는 동일하나,
    // 초기화 후 다시 동일한 엔티티를 읽은 경우 다른 객체가 반환된다.
    // 이런경우에 기본키 ID값 비교를 통해 Equals를 재정의 해야한다.

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null || Hibernate.getClass(this) != Hibernate.getClass(obj)){
            return false;
        }
        Account account = (Account) obj;
        return id != null && Objects.equals(id,account.id);
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
