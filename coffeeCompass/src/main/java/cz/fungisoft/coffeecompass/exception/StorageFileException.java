package cz.fungisoft.coffeecompass.exception;

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
