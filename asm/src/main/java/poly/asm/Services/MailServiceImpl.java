package poly.asm.Services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import poly.asm.Models.Mail;

@Service("mailService")
public class MailServiceImpl implements MailService{
    private final JavaMailSender mailSender;
    private final List<Mail> queue = new ArrayList<>();

    public MailServiceImpl(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    @Override
    public void send(String from, String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        mailSender.send(message);
    }

    @Override
    public void push(Mail mail) {
        queue.add(mail);
    }

    @Scheduled(fixedDelay = 500)
    public void run(){
        while (!queue.isEmpty()){
            try {
                Mail mail = queue.remove(0);
                send(mail.getFrom(), mail.getTo(), mail.getSubject(), mail.getBody());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
