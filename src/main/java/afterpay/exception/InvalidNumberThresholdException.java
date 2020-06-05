package afterpay.exception;

public class InvalidNumberThresholdException extends RuntimeException {
    public InvalidNumberThresholdException(String message) {
        super(message);
    }
    public InvalidNumberThresholdException(String message, Throwable cause) {
        super(message, cause);
    }
}