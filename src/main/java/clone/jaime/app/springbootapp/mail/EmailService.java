package clone.jaime.app.springbootapp.mail;

import org.springframework.stereotype.Service;


@Service
public interface EmailService {
    void sendEmail(EmailMessage emailMessage);

}
