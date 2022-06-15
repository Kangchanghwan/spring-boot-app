package clone.jaime.app.springbootapp.server.event.domain.entity;

import clone.jaime.app.springbootapp.server.account.domain.UserAccount;
import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.event.endpoint.form.EventForm;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//모임
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
public class Event {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")

    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    //모임 설명
    private String description;
    //만들 날짜
    private LocalDateTime createdDateTime;
    // 모집시작일시
    private LocalDateTime endEnrollmentDateTime;
    //모임 시작일시
    private LocalDateTime startDateTime;
    //모임 종료일시
    private LocalDateTime endDateTime;
    // 모집 인원 제한
    private Integer limitOfEnrollments;
    // 모임 참가자들
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
        event.endDateTime = eventForm.getEndDateTime();
        event.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        event.title = eventForm.getTitle();
        event.createdBy = account;
        event.study = study;
        event.createdDateTime = LocalDateTime.now();
        return event;
    }

    // 닫지 않은 모임에 가입되어 있지 않은경우
    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClose() && !isAlreadyEnrolled(userAccount);
    }

    // 닫지 않은 모임에 가입되어 있는 경우
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClose() && isAlreadyEnrolled(userAccount);
    }

    public boolean isNotClose() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
        // 모집일 만료 여부
    }

    //이미 가입 여부 확인
    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    //참석 여부
    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account) && enrollment.isAttend()) {
                return true;
            }
        }
        return false;
    }

    //현재 가입자 수 확인
    public int numberOfRemainSpots() {
        int accepted = (int) this.enrollments.stream()
                .filter(Enrollment::isAttend)
                .count();
        return this.limitOfEnrollments - accepted;
    }

    public Long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream()
                .filter(Enrollment::isAccepted)
                .count();
        // 모임 인원 들중 확정자 수
    }
    //참가자 수?

    public void updateFrom(EventForm eventForm) {
        this.title = eventForm.getTitle();
        this.description = eventForm.getDescription();
        this.eventType = eventForm.getEventType();
        this.startDateTime = eventForm.getStartDateTime();
        this.endDateTime = eventForm.getEndDateTime();
        this.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        this.endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
    }

    //대기중인 참가 신청 수용 가능여부
    public void acceptWaitingList() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            List<Enrollment> waitingList = this.enrollments.stream()
                    .filter(e -> e.isAccepted())
                    .collect(Collectors.toList());
            //모임 확정자를 제외한 나머지 리스트를 구한다. (대기 리스트)
            int numberToAccept = (int) Math.min(limitOfEnrollments - getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(Enrollment::accept);
        }
    }

    // 대기가 가능한지 여부 (선착순인 모임이 인원이 다 차있는 경우만 가능)
    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > getNumberOfAcceptedEnrollments();
    }

    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.attach(this);
    }

    public void removeEnrollment(Enrollment enrollment){
        this.enrollments.remove(enrollment);
        enrollment.detachEvent();
    }


    public void acceptNextIfAvailable() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            this.firstWaitingEnrollment().ifPresent(Enrollment::accept);
        }
    }

    private Optional<Enrollment> firstWaitingEnrollment() {
        return this.enrollments.stream()
                .filter(e -> e.isAccepted())
                .findFirst();

    }
}
