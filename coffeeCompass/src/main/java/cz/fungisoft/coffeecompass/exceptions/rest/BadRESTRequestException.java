package cz.fungisoft.coffeecompass.exceptions.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when a request to not allowed or to not available resource/endpoint is done.
 * 
 * @author Michal Vaclavek
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRESTRequestException extends RESTException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 2301176568015122676L;

    public BadRESTRequestException(String message) {
        super(message);
    }

    public BadRESTRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
