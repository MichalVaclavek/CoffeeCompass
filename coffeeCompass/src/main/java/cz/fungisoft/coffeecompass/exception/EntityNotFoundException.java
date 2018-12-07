package cz.fungisoft.coffeecompass.exception;

import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Zatim moc nepouzita vyjimka pro pripady, kdy neni v DB nalezena hledany objekt/polozka User nebo CoffeeSite.
 * 
 * @author Michal Vaclavek
 */
@ResponseStatus
public class EntityNotFoundException extends RuntimeException
{
    private static final long serialVersionUID = 6647479455776075743L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
