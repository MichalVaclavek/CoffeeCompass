package cz.fungisoft.users.services.tokens;

import cz.fungisoft.users.entity.PasswordResetToken;
import cz.fungisoft.users.entity.User;
import cz.fungisoft.users.entity.UserEmailVerificationToken;
import cz.fungisoft.users.repository.PasswordResetTokenRepository;
import cz.fungisoft.users.repository.UserEmailVerificationTokenRepository;
import cz.fungisoft.users.services.ISendEmailService;
import cz.fungisoft.users.services.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.UUID;

/**
 * Service implementing creation and sending token links for verification<br>
 * of the newly created user's e-mail and for resetting user's password.
 * 
 * @author Michal Vaclavek
 */
@Service("tokenCreateAndSendByEmail")
public class TokenCreateAndSendEmailSrvImpl implements TokenCreateAndSendEmailService {

    private User user;
    
    private Locale locale;
    
    private final UserService userService;
    
    private final ISendEmailService sendEmailService;
    
    private final MessageSource messages;
    
    private final UserEmailVerificationTokenRepository userEmailVerificationTokenRepository;
    
    private final PasswordResetTokenRepository passwordResetTokenRepository;

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
    
    @Override
    public String createAndSendVerificationTokenEmail(User user, Locale locale) {
        this.user = user;
        if (locale != null) {
            this.locale = locale;
        }

        if (this.user != null) {
            String token = UUID.randomUUID().toString();

            createUserVerificationToken(this.user, token);
            
            String recipientAddress = this.user.getEmail();
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
     */
    @Override
    public String reSendUserVerificationTokenEmail(String existingToken, Locale locale) {
        
        UserEmailVerificationToken newToken = generateNewUserVerificationToken(existingToken);
        // user's token is refreshed now (old user's token is deleted)
        this.user = userService.getUserByRegistrationToken(newToken.getToken());
        
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentRequest();
        UriComponentsBuilder extBuilder = builder.replacePath("/user/registrationConfirm").replaceQuery("token=" + newToken.getToken());
        String confirmationUrl = extBuilder.build().toUriString();
        
        String subject = messages.getMessage("register.verificationemail.resendToken.subject", null, locale);
        String message = messages.getMessage("register.verificationemail.resendToken.message", null, locale);
        String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
        
        this.sendEmailService.sendEmail(fromEmail, this.user.getEmail(), subject, message + "\r\n\r\n" + confirmationUrl);
        
        return newToken.getToken();
    }
    
    @Override
    public void createUserVerificationToken(User user, String token) {
        UserEmailVerificationToken myToken = new UserEmailVerificationToken(token, user);
        userEmailVerificationTokenRepository.save(myToken);
    }
    
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

    @Transactional
    @Override
    public void deleteRegistrationTokenByUser(User user) {
        userEmailVerificationTokenRepository.deleteByUser(user);
    }

    // ---- Password reset token ------  //
    
    @Override
    public void setResetPasswordTokenData(String userEmail, Locale locale) {
        user = userService.findByEmail(userEmail).orElse(null);
        this.locale = locale;
    }

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
