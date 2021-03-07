package cz.fungisoft.coffeecompass.serviceimpl.email;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.service.email.ISendEmailService;

@Service("sendMeEmailService")
public class SendEmailServiceImpl implements ISendEmailService
{
    private static final Logger logger = LogManager.getLogger(SendEmailServiceImpl.class);
    
    public JavaMailSender emailSender;

    @Autowired
    public SendEmailServiceImpl(JavaMailSender emailSender) {
        super();
        this.emailSender = emailSender;
    }

    @Override
    public void sendMeSimpleEmail(String fromName, String authorEmail, String fromEmail, String toEmail, String messageText) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Zpráva z CoffeeCompass, od: " + fromName + " , email: " + authorEmail); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("ODESLÁNO - zpráva z CoffeeCompass, od: {} , email: {}", fromName, authorEmail);
        }
        catch (MailException e) {
            logger.error("Chyba při odesílaní 'Contact me' e-mailu z adresy: {}. Exception: {}", authorEmail, e.getMessage());
            throw e;
        }
    }

    @Override
    public void sendEmail(String from, String to, String subject, String messageText) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("E-mail sent from {} to: {}. Subject: {}", from, to, subject);
        }
        catch (MailException e) {
            logger.error("Chyba při odesílaní e-mailu na adresu {} z {}. Exception: {}", to, from, e.getMessage());
            throw e;
        }
    }
    
}
