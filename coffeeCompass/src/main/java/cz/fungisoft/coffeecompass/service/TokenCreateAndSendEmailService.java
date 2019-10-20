package cz.fungisoft.coffeecompass.service;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import cz.fungisoft.coffeecompass.dto.UserDTO;
import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;

/**
 * Service to create user verification token and to send e-mail with verification link to a User.
 * 
 * @author Michal Vaclavek
 *
 */
public interface TokenCreateAndSendEmailService
{
    /**
     * Sets basic data needed to create and sent verification token e-mail.
     * 
     * @param user
     * @param appUrl
     * @param locale
     */
    public void setUserVerificationData(User user, String appUrl, Locale locale);
    
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
    public String reSendUserVerificationTokenEmail(String existingToken);
    
    // -------- User registration and verification by token and e-mail ---------- //

    public void createUserVerificationToken(User user, String token);
    
    public UserVerificationToken generateNewUserVerificationToken(String existingToken); 
    
    public UserVerificationToken getUserVerificationToken(String verificationToken);
    
    public void deleteRegistrationToken(String token);
    
    // ---- Reset password token and e-mail ---- //
    
    /**
     * Sets basic data needed to create reset password token
     * 
     * @param user
     * @param appUrl
     * @param locale
     */
    public void setResetPasswordTokenData(String userEmail, String appUrl, Locale locale);
    
    public void createPasswordResetToken(User user, String token);
    
    public String createAndSendResetPasswordTokenEmail();
    
    public PasswordResetToken getPasswordResetToken(String passwordResetToken);
    
    public void deletePasswordResetToken(String token);
}
