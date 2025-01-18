package site.radio.error;

public class ExternalApiFallbackException extends RuntimeException{
    public ExternalApiFallbackException(String message) {
        super(message);
    }
}
