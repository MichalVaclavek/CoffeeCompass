package cz.fungisoft.coffeecompass.exception;

/**
 * General REST Exception
 * 
 * @author Michal Vaclavek
 *
 */
public class RESTException extends RuntimeException
{
    
    private static final long serialVersionUID = 9071059486273080298L;
    
    private String localizedMessageCode;

    public String getLocalizedMessageCode() {
        return localizedMessageCode;
    }

    public void setLocalizedMessageCode(String localizedMessageCode) {
        this.localizedMessageCode = localizedMessageCode;
    }

    public RESTException(String message) {
        super(message);
    }

    public RESTException(String message, Throwable cause) {
        super(message, cause);
    }
}