package cz.fungisoft.coffeecompass.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    
    private String localizedMessageCode;
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

    public String getLocalizedMessageCode() {
        return localizedMessageCode;
    }

    public void setLocalizedMessageCode(String localizedMessageCode) {
        this.localizedMessageCode = localizedMessageCode;
    }
}
