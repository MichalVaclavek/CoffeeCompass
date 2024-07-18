package cz.fungisoft.users.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadAuthorizationRequestException extends RuntimeException {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3118897765883985419L;
    
    private String localizedMessageCode;
    

    public BadAuthorizationRequestException(String message) {
        super(message);
    }

    public BadAuthorizationRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public String getLocalizedMessageCode() {
        return localizedMessageCode;
    }

    public void setLocalizedMessageCode(String localizedMessageCode) {
        this.localizedMessageCode = localizedMessageCode;
    }
}
