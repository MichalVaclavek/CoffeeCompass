package cz.fungisoft.coffeecompass.serviceimpl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    public void sendMeSimpleEmail(String fromName, String fromEmail, String toEmail, String messageText) {
        SimpleMailMessage message = new SimpleMailMessage(); 
        message.setTo(toEmail);
        message.setSubject("Zprava z CoffeeCompass, od: " + fromName + " , email: " + fromEmail); 
        message.setText(messageText);
        try {
            emailSender.send(message);
            logger.info("Zprava z CoffeeCompass. Od: " + fromName + " , email: " + fromEmail);
        }
        catch (MailException e)
        {
            logger.error("Chyba pri odesilani zpravy: " + e.getMessage());
        }
    }
    
}
