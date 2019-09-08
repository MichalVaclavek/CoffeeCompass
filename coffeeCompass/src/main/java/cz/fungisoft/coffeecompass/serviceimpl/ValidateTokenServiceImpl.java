/**
 * 
 */
package cz.fungisoft.coffeecompass.serviceimpl;

import java.util.Calendar;

import org.springframework.stereotype.Service;

import cz.fungisoft.coffeecompass.entity.PasswordResetToken;
import cz.fungisoft.coffeecompass.entity.User;
import cz.fungisoft.coffeecompass.entity.UserVerificationToken;
import cz.fungisoft.coffeecompass.repository.PasswordResetTokenRepository;
import cz.fungisoft.coffeecompass.service.TokenCreateAndSendEmailService;
import cz.fungisoft.coffeecompass.service.ValidateTokenService;

/**
 * Service implementing tokens validating interface.
 * Implements validation of both user registration and password reset tokens.
 * 
 * @author Michal Vaclavek
 *
 */
@Service
public class ValidateTokenServiceImpl implements ValidateTokenService
{
    private PasswordResetTokenRepository passwordTokenRepository;
    
    private TokenCreateAndSendEmailService userVerificationTokenService;
    
    /**
     * 
     * @param passwordTokenRepository
     * @param userVerificationTokenService
     */
    public ValidateTokenServiceImpl(PasswordResetTokenRepository passwordTokenRepository,
                                    TokenCreateAndSendEmailService userVerificationTokenService) {
        super();
        this.passwordTokenRepository = passwordTokenRepository;
        this.userVerificationTokenService = userVerificationTokenService;
    }

    @Override
    public String validatePasswordResetToken(long id, String token) {
        
        PasswordResetToken passToken = passwordTokenRepository.findByToken(token);
        if ((passToken == null) || (passToken.getUser().getId() != id)) {
            return "invalidToken";
        }
   
        // Token expired ?
        Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return "expiredToken";
        }
   
        User user = passToken.getUser();
        
        if (user.getId() != id) {
            return "invalidUser"; 
        }
        
        // token valid
        return "";
    }

 
    @Override
    public String validateUserRegistrationToken(String token) {
        
        UserVerificationToken verificationToken = userVerificationTokenService.getUserVerificationToken(token);
        
        // Invalid token
        if (verificationToken == null) {
            return "invalidToken";
        }

        // Token expired ?
        Calendar cal = Calendar.getInstance();
        if ((verificationToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
            return "expiredToken";
        }
        // token valid
        return "";
    }

}
