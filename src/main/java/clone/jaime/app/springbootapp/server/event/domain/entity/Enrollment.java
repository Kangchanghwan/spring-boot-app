package clone.jaime.app.springbootapp.server.event.domain.entity;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

//참가자
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of="id")
@ToString
@Getter
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    //참가한 모임
    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;
    //참가자 정보
    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
    //참가한 시간
    private LocalDateTime enrolledAt;
    //확정 여부
    private boolean accepted;
    //참가 여부
    private boolean attend;

    public static Enrollment of(LocalDateTime enrolledAt, boolean isAbleToAcceptWaitingEnrollment, Account account) {
        Enrollment enrollment = new Enrollment();
        enrollment.enrolledAt = enrolledAt;
        enrollment.accepted = isAbleToAcceptWaitingEnrollment;
        enrollment.account = account;
        return enrollment;
    }


    public void attach(Event event) {
        this.event = event;
    }

    public void accept() {
        this.accepted = true;
    }

    public void detachEvent() {
        this.event = null;
    }
}
