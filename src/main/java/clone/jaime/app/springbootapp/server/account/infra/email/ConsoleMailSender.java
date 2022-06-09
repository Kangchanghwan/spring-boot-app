package clone.jaime.app.springbootapp.server.account.infra.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;

@Profile({"local", "local-db"})
//active profile이 local일 때만 빈에 등록되도록합니다.
@Component
//외부애서 주입해서 사용할 수 있도록 component를 사용한다.
@Slf4j
//로그 사용
public class ConsoleMailSender implements JavaMailSender {


    @Override
    public MimeMessage createMimeMessage() {
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        return null;
    }

    @Override
    public void send(MimeMessage mimeMessage) throws MailException {

    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {

    }

    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {

    }

    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {

    }

    @Override
    public void send(SimpleMailMessage simpleMessage) throws MailException {
        log.info("{}",simpleMessage);
        //simpleMailMessage를 파라미터로 받는 메서드를 구현한다.

    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {

    }
}
