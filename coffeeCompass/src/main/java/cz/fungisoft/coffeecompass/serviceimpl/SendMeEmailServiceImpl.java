package cz.fungisoft.coffeecompass.serviceimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.sun.activation.registries.MailcapParseException;

import cz.fungisoft.coffeecompass.service.ISendMeEmailService;

@Service("sendMeEmailService")
public class SendMeEmailServiceImpl implements ISendMeEmailService
{
    private static final Logger logger = LogManager.getLogger(SendMeEmailServiceImpl.class);
    
    public JavaMailSender emailSender;

    @Autowired
    public SendMeEmailServiceImpl(JavaMailSender emailSender) {
        super();
        this.emailSender = emailSender;
    }

    @Override
    public void sendMeSimpleEmail(String from, String fromEmail, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo("vaclavek.michal@gmail.com"); 
        message.setSubject("Zprava z CoffeeCompas. Od: " + from + " , email: " + fromEmail); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("Zprava z CoffeeCompas. Od: " + from + " , email: " + fromEmail);
        }
        catch (MailException e)
        {
            logger.error("Chyba pri odesilani zpravy: " + e.getMessage());
        }
    }
    
}
