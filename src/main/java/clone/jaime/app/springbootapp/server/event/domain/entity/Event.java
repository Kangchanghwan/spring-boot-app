package clone.jaime.app.springbootapp.server.event.domain.entity;

import clone.jaime.app.springbootapp.server.account.domain.UserAccount;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.event.endpoint.form.EventForm;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

//모임
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")

    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    private LocalDateTime createdDateTime;

    private LocalDateTime endEnrollmentDateTime;

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public static Event From(EventForm eventForm, Account account, Study study) {
        Event event = new Event();
        event.eventType = eventForm.getEventType();
        event.description = eventForm.getDescription();
        event.startDateTime = eventForm.getStartDateTime();
        event.endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
        event.endDateTime  = eventForm.getEndDateTime();
        event.limitOfEnrollments  = eventForm.getLimitOfEnrollments();
        event.title  = eventForm.getTitle();
        event.createdBy = account;
        event.study = study;
        event.createdDateTime = LocalDateTime.now();
        return event;
    }

    // 닫지 않은 모임에 가입되어 있지 않은경우
    public boolean isEnrollableFor(UserAccount userAccount){
        return isNotClose() && !isAlreadyEnrolled(userAccount);
    }

    // 닫지 않은 모임에 가입되어 있는 경우
    public boolean isDisenrollableFor(UserAccount userAccount){
        return isNotClose() && isAlreadyEnrolled(userAccount);
    }

    public  boolean isNotClose(){
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
        // 모집일 만료 여부
    }

    //이미 가입 여부 확인
    private boolean isAlreadyEnrolled(UserAccount userAccount){
        Account account = userAccount.getAccount();
        for(Enrollment enrollment : this.enrollments){
            if(enrollment.getAccount().equals(account)){
                return true;
            }
        }
        return false;
    }

    //참석 여부
    public boolean isAttended(UserAccount userAccount){
        Account account = userAccount.getAccount();
        for(Enrollment enrollment : this.enrollments){
            if(enrollment.getAccount().equals(account) && enrollment.isAttend()){
                return true;
            }
        }
        return false;
    }

}
