package cz.fungisoft.coffeecompass.service;

/**
 * For sending "Contact me" e-mail messages and
 * For sending verification e-mail to new User.
 * For sending reset password email link. 
 * 
 * @author Michal Vaclavek
 *
 */
public interface ISendEmailService
{
    /**
     * 
     * @param fromName - contact me message author's name inserted to Contact me form
     * @param authorEmail - contact me message author's email inserted to Contact me form
     * @param fromEmail - from email address of the Contact me message (usually read from Configuration)
     * @param toEmail - email address where to send e-mail (usually read from Configuration)
     * @param message
     */
    public void sendMeSimpleEmail(String fromName, String authorEmail, String fromEmail, String toEmail, String message);
    
    public void sendEmail(String from, String to, String subject, String message);
}
