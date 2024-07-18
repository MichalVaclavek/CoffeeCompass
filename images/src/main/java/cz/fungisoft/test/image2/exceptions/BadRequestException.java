package cz.fungisoft.test.image2.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when a request to not allowed or to not available resource/endpoint is done.
 * 
 * @author Michal Vaclavek
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RESTException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 2301176568015122676L;

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
