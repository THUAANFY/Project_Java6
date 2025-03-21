package poly.asm.Services;

import poly.asm.Models.Mail;
import jakarta.mail.MessagingException;

public interface MailService {
    void send(String from, String to, String subject, String body)
    throws MessagingException;

    void push(Mail mail);

    default void push(String from, String to, String subject, String body){
        this.push(Mail.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .body(body)
                    .build());
    }
}
