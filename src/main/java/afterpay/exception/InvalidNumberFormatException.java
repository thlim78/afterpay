package afterpay.exception;

public class InvalidNumberFormatException extends RuntimeException {
    public InvalidNumberFormatException(String message) {
        super(message);
    }
    public InvalidNumberFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}