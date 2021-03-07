package cz.fungisoft.coffeecompass.serviceimpl.tokens;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.UUID;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserEmailVerificationToken;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.repository.UserEmailVerificationTokenRepository;
import cz.fungisoft.coffeecompass.service.email.ISendEmailService;
import cz.fungisoft.coffeecompass.service.tokens.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.user.UserService;

/**
 * 
 * Service implementing creation and sending token links for verification<br>
 * of the newly created user's e-mail and for reseting user's password.
 * 
 * @author Michal Vaclavek
 */
@Service("tokenCreateAndSendByEmail")
public class TokenCreateAndSendEmailSrvImpl implements TokenCreateAndSendEmailService
{
    private User user;
    
    private Locale locale;
    
    private UserService userService;
    
    private ISendEmailService sendEmailService;
    
    private MessageSource messages;
    
    private UserEmailVerificationTokenRepository userEmailVerificationTokenRepository;
    
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
                                          UserEmailVerificationTokenRepository userEmailVerificationTokenRepository,
                                          PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
        this.messages = messagesSource;
        this.userEmailVerificationTokenRepository = userEmailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.locale = LocaleContextHolder.getLocale();
    }
    
    /**
     * Enters User data and locale (to get correct language for the e-mail texts) to send
     * e-mail for user's e-mail address validation.
     * Locale can be null
     */
    @Override
    public void setUserVerificationData(User user, Locale locale) {
        this.user = user;
        if (locale != null) {
            this.locale = locale;
        }
    }
       
    @Override
    public String createAndSendVerificationTokenEmail() {
        String token = UUID.randomUUID().toString();
        
        if (user != null) {
            createUserVerificationToken(user, token);
            
            String recipientAddress = user.getEmail();
            String subject = messages.getMessage("register.verificationemail.subject", null, locale);
            String message = messages.getMessage("register.verificationemail.message", null, locale);
            
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
            UriComponentsBuilder extBuilder = builder.replacePath("/user/registrationConfirm").replaceQuery("token=" + token);
            String confirmationUrl = extBuilder.build().toUriString();
            
            String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
            
            //TODO throw exception in case of e-mail sent error
            this.sendEmailService.sendEmail(fromEmail, recipientAddress, subject, message + "\r\n" + confirmationUrl);
            
            return token;
        } else {
            throw new InvalidParameterException("User is null.");
        }
    }

    /**
     * Creates a new verification token and send it by e-mail.
     * 
     */
    @Override
    public String reSendUserVerificationTokenEmail(String existingToken) {
        
        UserEmailVerificationToken newToken = generateNewUserVerificationToken(existingToken);
        this.user = userService.getUserByRegistrationToken(newToken.getToken());
        
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.replacePath("/user/registrationConfirm").replaceQuery("token=" + newToken.getToken());
        String confirmationUrl = extBuilder.build().toUriString();
        
        String subject = messages.getMessage("register.verificationemail.resendToken.subject", null, locale);
        String message = messages.getMessage("register.verificationemail.resendToken.message", null, locale);
        String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
        
        this.sendEmailService.sendEmail(fromEmail, user.getEmail(), subject, message + "\r\n\r\n" + confirmationUrl);
        
        return newToken.getToken();
    }
    
    @Transactional
    @Override
    public void createUserVerificationToken(User user, String token) {
        UserEmailVerificationToken myToken = new UserEmailVerificationToken(token, user);
        userEmailVerificationTokenRepository.save(myToken);
    }
    
    @Transactional
    @Override
    public UserEmailVerificationToken generateNewUserVerificationToken(String existingToken) {
        UserEmailVerificationToken oldToken = getUserVerificationToken(existingToken);
        UserEmailVerificationToken myToken = new UserEmailVerificationToken(oldToken.getToken(), oldToken.getUser());
        userEmailVerificationTokenRepository.delete(oldToken);
        userEmailVerificationTokenRepository.save(myToken);
        return myToken;
    }

    @Override
    public UserEmailVerificationToken getUserVerificationToken(String verificationToken) {
        return userEmailVerificationTokenRepository.findByToken(verificationToken);
    }
    
    @Transactional
    @Override
    public void deleteRegistrationToken(String token) {
        userEmailVerificationTokenRepository.deleteByToken(token);
    }

    // ---- Password reset token ------  //
    
    @Override
    public void setResetPasswordTokenData(String userEmail, Locale locale) {
        user = userService.findByEmail(userEmail).orElse(null);
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
            
            ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
            UriComponentsBuilder extBuilder = builder.replacePath("/user/changePassword").replaceQuery("userId=" + user.getId() + "&token=" + token);
            String resetPasswordUrl = extBuilder.build().toUriString();
            
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
