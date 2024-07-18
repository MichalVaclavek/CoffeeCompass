package cz.fungisoft.users.services.tokens;


import cz.fungisoft.users.entity.PasswordResetToken;
import cz.fungisoft.users.entity.User;
import cz.fungisoft.users.entity.UserEmailVerificationToken;

import java.util.Locale;

/**
 * Service to create user verification token and to send e-mail with verification link to a User.
 * 
 * @author Michal Vaclavek
 */
public interface TokenCreateAndSendEmailService {

    /**
     * Creates and send verification token e-mail to confirm user's e-mail address.
     * 
     * @return created token string
     */
    String createAndSendVerificationTokenEmail(User user, Locale locale);

    /**
     * Creates and send new verification token e-mail to confirm user's e-mail address.
     * 'Old', existing token is deleted from DB.
     * 
     * @return created new token string based on existing token (and it's user)
     */
    String reSendUserVerificationTokenEmail(String existingToken, Locale locale);
    
    // -------- User registration and verification by token and e-mail ---------- //

    void createUserVerificationToken(User user, String token);
    
    UserEmailVerificationToken generateNewUserVerificationToken(String existingToken);
    
    UserEmailVerificationToken getUserVerificationToken(String verificationToken);
    
    void deleteRegistrationToken(String token);

    void deleteRegistrationTokenByUser(User user);

    // ---- Reset password token and e-mail ---- //
    
    /**
     * Sets basic data needed to create reset password token
     * 
     * @param userEmail
     * @param locale
     */
    void setResetPasswordTokenData(String userEmail, Locale locale);
    
    void createPasswordResetToken(User user, String token);
    
    String createAndSendResetPasswordTokenEmail();
    
    PasswordResetToken getPasswordResetToken(String passwordResetToken);
    
    void deletePasswordResetToken(String token);
}
