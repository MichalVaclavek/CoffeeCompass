package cz.fungisoft.coffeecompass.serviceimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.service.ISendEmailService;

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
    public void sendMeSimpleEmail(String fromName, String fromEmail, String toEmail, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo(toEmail);
        message.setSubject("Zpráva z CoffeeCompass, od: " + fromName + " , email: " + fromEmail); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("ODESLÁNO - zpráva z CoffeeCompass, od: " + fromName + " , email: " + fromEmail);
        }
        catch (MailException e)
        {
            logger.error("Chyba při odesílaní 'Contact-me' e-mailu: " + e.getMessage());
        }
    }

    @Override
    public void sendVerificationEmail(String from, String to, String subject, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo(to);
        message.setSubject(subject); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("Verification e-mail sent to: {}. Subject: {}", to, subject);
        }
        catch (MailException e)
        {
            logger.error("Chyba při odesílaní verification e-mailu: " + e.getMessage());
        }
    }
    
}
