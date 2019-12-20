package cz.fungisoft.coffeecompass.exceptions.rest;

/**
 * General REST Exception
 * 
 * @author Michal Vaclavek
 *
 */
public class RESTException extends RuntimeException
{
    
    private static final long serialVersionUID = 9071059486273080298L;
    
    protected String localizedErrorMessage;

    public String getLocalizedErrorMessage() {
        return localizedErrorMessage;
    }

    public void setLocalizedErrorMessage(String localizedMessage) {
        this.localizedErrorMessage = localizedMessage;
    }

    public RESTException(String message) {
        super(message);
    }

    public RESTException(String message, Throwable cause) {
        super(message, cause);
    }
}