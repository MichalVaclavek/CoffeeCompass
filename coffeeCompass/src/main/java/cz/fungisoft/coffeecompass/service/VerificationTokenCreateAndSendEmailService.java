package cz.fungisoft.coffeecompass.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;

/**
 * Service to create user verification token and to send e-mail with verification link to a User.
 * 
 * @author Michal Vaclavek
 *
 */
public interface VerificationTokenCreateAndSendEmailService
{
    /**
     * Sets basic data needed to create and sent verification token e-mail.
     * 
     * @param user
     * @param appUrl
     * @param locale
     */
    public void setVerificationData(User user, String appUrl, Locale locale);
    
    
    /**
     * Creates and send verification token e-mail to confirm user's e-mail address.
     * 
     * @return created token string
     */
    public String createAndSendVerificationTokenEmail();

    /**
     * Creates and send new verification token e-mail to confirm user's e-mail address.
     * 'Old', existing token is deleted from DB.
     * 
     * @return created new token string
     */
    public String reSendVerificationTokenEmail(String existingToken);
    
    // User registration and verification by token and e-mail

    public void createVerificationToken(User user, String token);
    
    public UserVerificationToken generateNewVerificationToken(String existingToken); 
    
    public UserVerificationToken getVerificationToken(String verificationToken);

}
