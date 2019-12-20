/**
 * 
 */
package cz.fungisoft.coffeecompass.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Not used yet.
 * 
 * @author Michal Václavek
 *
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="File Not Found Exception!")
public class FileNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = -6348014814749638612L;

    public FileNotFoundException(String message) {
        super(message);
    }

    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
