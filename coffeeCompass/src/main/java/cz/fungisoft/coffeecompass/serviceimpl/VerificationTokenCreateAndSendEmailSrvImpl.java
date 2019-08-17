package cz.fungisoft.coffeecompass.serviceimpl;

import java.security.InvalidParameterException;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.dto.UserDataDTO;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;
import cz.fungisoft.coffeecompass.repository.UserVerificationTokenRepository;
import cz.fungisoft.coffeecompass.service.ISendEmailService;
import cz.fungisoft.coffeecompass.service.UserService;
import cz.fungisoft.coffeecompass.service.VerificationTokenCreateAndSendEmailService;

@Service("verificationTokenSendEmail")
public class VerificationTokenCreateAndSendEmailSrvImpl implements VerificationTokenCreateAndSendEmailService
{
    private User user;
    
    private String appUrl;
    
    private Locale locale;
    
    private UserService userService;
    
    private ISendEmailService sendEmailService;
    
    private MessageSource messages;
    
    private UserVerificationTokenRepository tokenRepository;

    /**
     * Constructor
     * 
     * @param userService
     */
    @Autowired
    public VerificationTokenCreateAndSendEmailSrvImpl(UserService userService,
                                                      ISendEmailService sendEmailService,
                                                      MessageSource messagesSource,
                                                      UserVerificationTokenRepository tokenRepository) {
        this.userService = userService;
        this.sendEmailService = sendEmailService;
        this.messages = messagesSource;
        this.tokenRepository = tokenRepository;
    }
    
    @Override
    public void setVerificationData(User user, String appUrl, Locale locale) {
        this.user = user;
        this.appUrl = appUrl;
        this.locale = locale;
    }
       
    @Override
    public String createAndSendVerificationTokenEmail() {
        String token = UUID.randomUUID().toString();
        
        if (user != null) {
            createVerificationToken(user, token);
            
            String recipientAddress = user.getEmail();
            String subject = messages.getMessage("register.verificationemail.subject", null, locale);
            String message = messages.getMessage("register.verificationemail.message", null, locale);
            String confirmationUrl = appUrl + "/user/registrationConfirm?token=" + token;
            
            String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
            
            this.sendEmailService.sendVerificationEmail(fromEmail, recipientAddress, subject, message + "\r\n" + confirmationUrl);
            
            return token;
        } else
            throw new InvalidParameterException("User is null.");
    }

    /**
     * Creates a new verification token and send it by e-mail.
     * 
     */
    // TODO - what about to check if the e-mail was really send. Inform user via Exception?
    @Override
    public String reSendVerificationTokenEmail(String existingToken) {
        
        UserVerificationToken newToken = generateNewVerificationToken(existingToken);
        this.user = userService.getUserByToken(newToken.getToken());
        
        String confirmationUrl = appUrl + "/user/registrationConfirm?token=" + newToken.getToken();
        String subject = messages.getMessage("register.verificationemail.resendToken.subject", null, locale);
        String message = messages.getMessage("register.verificationemail.resendToken.message", null, locale);
        String fromEmail = messages.getMessage("register.verificationemail.from", null, locale);
        
        this.sendEmailService.sendVerificationEmail(fromEmail, user.getEmail(), subject, message + "\r\n" + confirmationUrl);
        
        return newToken.getToken();
    }
    
    @Override
    public void createVerificationToken(User user, String token) {
        UserVerificationToken myToken = new UserVerificationToken(token, user);
        tokenRepository.save(myToken);
    }
    
    @Override
    public UserVerificationToken generateNewVerificationToken(String existingToken) {
        UserVerificationToken oldToken = getVerificationToken(existingToken);
        UserVerificationToken myToken = new UserVerificationToken(oldToken.getToken(), oldToken.getUser());
        tokenRepository.delete(oldToken);
        tokenRepository.save(myToken);
        return myToken;
    }

    @Override
    public UserVerificationToken getVerificationToken(String verificationToken) {
        return tokenRepository.findByToken(verificationToken);
    }

}
