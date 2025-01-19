package site.radio.error;

public class ExternalApiUnhandledException extends RuntimeException{
    public ExternalApiUnhandledException(String message, Throwable cause) {
        super(message, cause);
    }
}
