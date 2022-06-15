package clone.jaime.app.springbootapp.server.event.application;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.event.domain.entity.Enrollment;
import clone.jaime.app.springbootapp.server.event.domain.entity.Event;
import clone.jaime.app.springbootapp.server.event.endpoint.form.EventForm;
import clone.jaime.app.springbootapp.server.event.infra.repository.EnrollmentRepository;
import clone.jaime.app.springbootapp.server.event.infra.repository.EventRepository;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(EventForm eventForm, Account account, Study study){
        Event event = Event.From(eventForm,account,study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        event.updateFrom(eventForm);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }


    public void enroll(Event event, Account account) {
        if(!enrollmentRepository.existsByEventAndAccount(event,account)){
            Enrollment enrollment = Enrollment.of(LocalDateTime.now(),event.isAbleToAcceptWaitingEnrollment(),account);
            event.addEnrollment(enrollment);
            enrollmentRepository.save(enrollment);
        }
    }

    public void leave(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event,account);
        event.removeEnrollment(enrollment);
        enrollmentRepository.delete(enrollment);
        event.acceptNextIfAvailable(); // 다음 참가자 확정 처리 메소드
    }
}
