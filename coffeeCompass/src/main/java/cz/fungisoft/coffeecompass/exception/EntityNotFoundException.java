package cz.fungisoft.coffeecompass.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Vyjimka pro pripady, kdy neni v DB nalezena hledany objekt/polozka, urceno prevazne pro objekty typu User nebo CoffeeSite.
 * 
 * @author Michal Vaclavek
 */
@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="Entity Not Found Exception!")
public class EntityNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = 6647479455776075743L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
