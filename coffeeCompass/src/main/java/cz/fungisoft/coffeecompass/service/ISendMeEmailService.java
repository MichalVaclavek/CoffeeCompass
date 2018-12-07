package cz.fungisoft.coffeecompass.service;

/**
 * For sending "Contact me" messages from the application
 * 
 * @author Michal Vaclavek
 *
 */
public interface ISendMeEmailService
{
    public void sendMeSimpleEmail(String fromName, String fromEmail, String toEmail, String message);
}
