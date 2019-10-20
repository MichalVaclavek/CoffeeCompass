package cz.fungisoft.coffeecompass.serviceimpl;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.repository.UserVerificationTokenRepository;
import cz.fungisoft.coffeecompass.service.ISendEmailService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;

/**
 * 
 * Service implementing creation and sending token links for verification of the newly created user's e-mail
 * and for reseting user's password.
 * 
 * @author Michal Vaclavek
 */
@Service("tokenCreateAndSendByEmail")
public class TokenCreateAndSendEmailSrvImpl implements TokenCreateAndSendEmailService
{
    private User user;
    
    private String appUrl;
    
    private Locale locale;
    
    private UserService userService;
    
    private ISendEmailService sendEmailService;
    
    private MessageSource messages;
    
    private UserVerificationTokenRepository userVerificationTokenRepository;
    
    private PasswordResetTokenRepository passwordResetTokenRepository;

    /**
     * Constructor
     * 
     * @param userService
     */
    @Autowired
    public TokenCreateAndSendEmailSrvImpl(UserService userService,
                                          ISendEmailService sendEmailService,
                                          MessageSource messagesSource,
                                          UserVerificationTokenRepository userVerificationTokenRepository,
                                          PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
        this.messages = messagesSource;
        this.userVerificationTokenRepository = userVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }
    
    @Override
    public void setUserVerificationData(User user, String appUrl, Locale locale) {
        this.user = user;
        this.appUrl = appUrl;
        this.locale = locale;
    }
       
    @Override
    public String createAndSendVerificationTokenEmail() {
        String token = UUID.randomUUID().toString();
        
        if (user != null) {
            createUserVerificationToken(user, token);
            
            String recipientAddress = user.getEmail();
            String subject = messages.getMessage("register.verificationemail.subject", null, locale);
            String message = messages.getMessage("register.verificationemail.message", null, locale);
            String confirmationUrl = appUrl + "/user/registrationConfirm?token=" + token;
            
            String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
            
            //TODO throw exception in case of e-mail sent error
            this.sendEmailService.sendEmail(fromEmail, recipientAddress, subject, message + "\r\n" + confirmationUrl);
            
            return token;
        } else
            throw new InvalidParameterException("User is null.");
    }

    /**
     * Creates a new verification token and send it by e-mail.
     * 
     */
    @Override
    public String reSendUserVerificationTokenEmail(String existingToken) {
        
        UserVerificationToken newToken = generateNewUserVerificationToken(existingToken);
        this.user = userService.getUserByRegistrationToken(newToken.getToken());
        
        String confirmationUrl = appUrl + "/user/registrationConfirm?token=" + newToken.getToken();
        String subject = messages.getMessage("register.verificationemail.resendToken.subject", null, locale);
        String message = messages.getMessage("register.verificationemail.resendToken.message", null, locale);
        String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
        
        this.sendEmailService.sendEmail(fromEmail, user.getEmail(), subject, message + "\r\n\r\n" + confirmationUrl);
        
        return newToken.getToken();
    }
    
    @Transactional
    @Override
    public void createUserVerificationToken(User user, String token) {
        UserVerificationToken myToken = new UserVerificationToken(token, user);
        userVerificationTokenRepository.save(myToken);
    }
    
    @Transactional
    @Override
    public UserVerificationToken generateNewUserVerificationToken(String existingToken) {
        UserVerificationToken oldToken = getUserVerificationToken(existingToken);
        UserVerificationToken myToken = new UserVerificationToken(oldToken.getToken(), oldToken.getUser());
        userVerificationTokenRepository.delete(oldToken);
        userVerificationTokenRepository.save(myToken);
        return myToken;
    }

    @Override
    public UserVerificationToken getUserVerificationToken(String verificationToken) {
        return userVerificationTokenRepository.findByToken(verificationToken);
    }
    
    @Transactional
    @Override
    public void deleteRegistrationToken(String token) {
        userVerificationTokenRepository.deleteByToken(token);
    }

    // ---- Password reset token ------  //
    
    @Override
    public void setResetPasswordTokenData(String userEmail, String appUrl, Locale locale) {
        user = userService.findByEmail(userEmail).orElse(null);
        this.appUrl = appUrl;
        this.locale = locale;
    }

    @Transactional
    @Override
    public void createPasswordResetToken(User user, String token) {
        PasswordResetToken resetPasswdToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetPasswdToken);
    }

    @Override
    public String createAndSendResetPasswordTokenEmail() {
        
        String token = UUID.randomUUID().toString();
        
        if (user != null) {
            createPasswordResetToken(user, token);
            
            String recipientAddress = user.getEmail();
            String subject = messages.getMessage("resetPassword.email.subject", null, locale);
            String message = messages.getMessage("resetPassword.email.message", null, locale);
            String fromEmail = messages.getMessage("resetPassword.email.from", null, locale);
            
            String resetPasswordUrl = appUrl + "/user/changePassword?userId=" + user.getId() + "&token=" + token;;
            
            this.sendEmailService.sendEmail(fromEmail, recipientAddress, subject, message + "\r\n\r\n" + resetPasswordUrl);
            
            return token;
        } else
            throw new InvalidParameterException("User is null.");
    }
    
    @Override
    public PasswordResetToken getPasswordResetToken(String passwordResetToken) {
        return passwordResetTokenRepository.findByToken(passwordResetToken);
    }

    @Transactional
    @Override
    public void deletePasswordResetToken(String token) {
        passwordResetTokenRepository.deleteByToken(token);
    }

}
