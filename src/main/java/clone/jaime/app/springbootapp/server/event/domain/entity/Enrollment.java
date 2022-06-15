package clone.jaime.app.springbootapp.server.event.domain.entity;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

//참가
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of="id")
@ToString
@Getter
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;


    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne (cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;
    private boolean attend;
}
