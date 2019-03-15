package cz.fungisoft.coffeecompass.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Not used yet.
 * 
 * @author Michal Václavek
 *
 */
@ResponseStatus(value=HttpStatus.INSUFFICIENT_STORAGE, reason="File cannot be saved!")
public class StorageFileException extends RuntimeException
{
    private static final long serialVersionUID = 6647429455776075744L;

    public StorageFileException(String message) {
        super(message);
    }
    
    public StorageFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
