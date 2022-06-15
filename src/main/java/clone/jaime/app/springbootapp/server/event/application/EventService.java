package clone.jaime.app.springbootapp.server.event.application;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.event.domain.entity.Event;
import clone.jaime.app.springbootapp.server.event.endpoint.form.EventForm;
import clone.jaime.app.springbootapp.server.event.infra.repository.EventRepository;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;


    public Event createEvent(EventForm eventForm, Account account, Study study){
        Event event = Event.From(eventForm,account,study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        event.updateFrom(eventForm);
    }
}
