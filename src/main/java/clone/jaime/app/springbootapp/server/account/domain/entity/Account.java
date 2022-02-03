package clone.jaime.app.springbootapp.server.account.domain.entity;


import clone.jaime.app.springbootapp.server.account.domain.entity.support.ListStringConverter;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
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

    private String emailToken;

    @Embedded
    private Profile profile;

    @Embedded
    private NotificationSetting notificationSetting;

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

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter
    @ToString
    public static class Profile {
        private String bio;
        @Convert(converter = ListStringConverter.class)
        private List<String> url;
        //list를 DB컬럼하나에 매핑하기위해 converter를 사용하였습니다.
        private String job;
        private String location;
        private String company;
        @Lob @Basic(fetch = FetchType.EAGER)
        private String image;
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter
    @ToString
    public static class NotificationSetting{
        private Boolean studyCreatedByEmail;
        private Boolean studyCreatedByWeb;
        private Boolean studyRegistrationResultByEmailByEmail;
        private Boolean studyRegistrationResultByEmailByWeb;
        private Boolean studyUpdatedByEmail;
        private Boolean studyUpdatedByWeb;

    }


}
