package clone.jaime.app.springbootapp.server.event.endpoint.form;

import clone.jaime.app.springbootapp.server.event.domain.entity.Event;
import clone.jaime.app.springbootapp.server.event.domain.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventForm {

    @NotBlank
    @Length(max=50)
    private String title;

    private String description;

    private EventType eventType = EventType.FCFS;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    //가장 일반적인 ISO 날짜 시간 형식 yyyy-MM-dd'T'HH:mm:ss.SSSXXX — 예: "2000-10-31T01:30:00.000-05:00".
    private LocalDateTime endEnrollmentDateTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @Min(2)
    private Integer limitOfEnrollments = 2;


    public static EventForm from(Event event){
        EventForm eventForm = new EventForm();
        eventForm.title = event.getTitle();
        eventForm.description = event.getDescription();
        eventForm.eventType = event.getEventType();
        eventForm.endEnrollmentDateTime = event.getEndEnrollmentDateTime();
        eventForm.startDateTime = event.getStartDateTime();
        eventForm.endDateTime = event.getEndDateTime();
        eventForm.limitOfEnrollments = event.getLimitOfEnrollments();
        return eventForm;
    }


}
