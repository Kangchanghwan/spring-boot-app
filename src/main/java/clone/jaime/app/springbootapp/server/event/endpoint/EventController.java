package clone.jaime.app.springbootapp.server.event.endpoint;

import clone.jaime.app.springbootapp.server.account.domain.entity.Account;
import clone.jaime.app.springbootapp.server.account.domain.entity.support.CurrentUser;
import clone.jaime.app.springbootapp.server.event.application.EventService;
import clone.jaime.app.springbootapp.server.event.domain.entity.Event;
import clone.jaime.app.springbootapp.server.event.endpoint.form.EventForm;
import clone.jaime.app.springbootapp.server.event.infra.repository.EventRepository;
import clone.jaime.app.springbootapp.server.event.validator.EventValidator;
import clone.jaime.app.springbootapp.server.study.application.StudyService;
import clone.jaime.app.springbootapp.server.study.domain.entity.Study;
import clone.jaime.app.springbootapp.server.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}")
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;

    private final StudyRepository studyRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }




    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account,
                                  @PathVariable String path,
                                  Model model){
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);

        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        for(Event event : events){
            if(event.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(event);
            }else{
                newEvents.add(event);
            }
        }
        model.addAttribute("newEvents",newEvents);
        model.addAttribute("oldEvents",oldEvents);
        return "event/events";
    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account,
                           @PathVariable String path,
                           @PathVariable Long id,
                           Model model){
        model.addAttribute(account);
        model.addAttribute(eventRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 모임은 존재하지 않습니다.")
        ));
        model.addAttribute(studyRepository.findStudyWithManagersByPath(path));
        return "event/view";
    }


    @PostMapping("/new-event")
    public String createdNewEvent(@CurrentUser Account account,
                                  @PathVariable String path,
                                  @Valid EventForm eventForm,
                                  Errors errors,
                                  Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }
        Event event = eventService.createEvent(eventForm,account,study);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account,
                               @PathVariable String path,
                               Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }


    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account,
                               @PathVariable String path,
                               @PathVariable Long id,
                               Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("모임이 존재하지 않습니다."));
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(new EventForm());
        return "event/update-form";
    }

    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account,
                                  @PathVariable String path,
                                  @PathVariable Long id,
                                  @Valid EventForm eventForm,
                                  Errors errors,
                                  Model model){
        Study study = studyService.getStudyToUpdateStatus(account,path);
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("모임이 존재하지 않습니다."));
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm,event,errors);
        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }
        eventService.updateEvent(event,eventForm);
        return "redirect:/study/" + study.getEncodePath() + "/events/" + event.getId();
    }

}
