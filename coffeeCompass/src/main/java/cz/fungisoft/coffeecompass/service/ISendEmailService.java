package cz.fungisoft.coffeecompass.service;

/**
 * For sending "Contact me" e-mail messages and
 * for sending verification e-mail to new User.
 * 
 * @author Michal Vaclavek
 *
 */
public interface ISendEmailService
{
    public void sendMeSimpleEmail(String fromName, String fromEmail, String toEmail, String message);
    
    public void sendEmail(String from, String to, String subject, String message);
}
