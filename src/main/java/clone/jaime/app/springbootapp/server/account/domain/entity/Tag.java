package clone.jaime.app.springbootapp.server.account.domain.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@Builder
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true, nullable = false)
    private String title;
}
