/**
 * 
 */
package cz.fungisoft.coffeecompass.exception;

/**
 * @author Michal
 *
 */
public class MyFileNotFoundException extends RuntimeException
{
    public MyFileNotFoundException(String message) {
        super(message);
    }

    public MyFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
