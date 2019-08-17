package cz.fungisoft.coffeecompass.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Vyjimka pro pripady, kdy neni v DB nalezena hledany objekt typu User.
 * 
 * @author Michal Vaclavek
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="User Not Found Exception!")
public class UserNotFoundException extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = -3953771079191608961L;

    public UserNotFoundException(String message) {
        super(message);
    }
}
