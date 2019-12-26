package cz.fungisoft.coffeecompass.exceptions.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when a request is performed to secured endpoints
 * without user authentication/authorization.
 * 
 * @author Michal Vaclavek
 *
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class BadAuthorizationRESTRequestException extends RESTException
{
    
    /**
     * 
     */
    private static final long serialVersionUID = -3113440066964755013L;
    
    public BadAuthorizationRESTRequestException(String message) {
        super(message);
    }

    public BadAuthorizationRESTRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
