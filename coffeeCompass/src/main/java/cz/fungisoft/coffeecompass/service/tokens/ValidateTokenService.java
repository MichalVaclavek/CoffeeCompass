package cz.fungisoft.coffeecompass.service.tokens;

/**
 * Used to validate user registration and reset password tokens
 * 
 * @author Michal Vaclavek
 *
 */
public interface ValidateTokenService {

    String TOKEN_EXPIRED = "expiredToken";
    
    String TOKEN_INVALID = "invalidToken";
    
    String TOKEN_INVALID_USER =  "invalidTokenUser";
    
    /**
     * Validates password reset token sent to user on request.
     * 
     * @param id - id of the user requesting password reset.
     * @param token - token to be validated
     * 
     * @return empty string if token is valid, otherwise invalid token reason i.e. "expiredToken" or "invalidToken" or "invalidUser"
     */
    String validatePasswordResetToken(long id, String token);
    
    /**
     * Validates registration confirm e-mail token sent to user's email during registration process.
     * 
     * @param token - token to be validated
     * 
     * @return empty string if token is valid, otherwise invalid token reason i.e. "expiredToken" or "invalidToken"
     */
    String validateUserRegistrationToken(String token);
}
