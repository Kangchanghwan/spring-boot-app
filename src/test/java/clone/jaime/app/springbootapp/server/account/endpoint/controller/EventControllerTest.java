package clone.jaime.app.springbootapp.server.account.endpoint.controller;

import clone.jaime.app.springbootapp.server.event.application.EventService;
import clone.jaime.app.springbootapp.server.event.infra.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class EventControllerTest {

    @MockBean
    MockMvc mockMvc;
    @Autowired
    EventService eventService;
    @Autowired
    EventRepository eventRepository;



}