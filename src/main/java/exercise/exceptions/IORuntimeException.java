package exercise.exceptions;

/**
 * Custom exception. There is no possibility to recover from IO when reading or writing to file, so RuntimeException.
 */
public class IORuntimeException extends RuntimeException {
    public IORuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
