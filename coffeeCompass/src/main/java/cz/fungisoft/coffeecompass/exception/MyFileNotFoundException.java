/**
 * 
 */
package cz.fungisoft.coffeecompass.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Not used yet.
 * 
 * @author Michal Václavek
 *
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="File Not Found Exception!")
public class MyFileNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = -6348014814749638612L;

    public MyFileNotFoundException(String message) {
        super(message);
    }

    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
