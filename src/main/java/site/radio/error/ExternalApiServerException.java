package site.radio.error;

public class ExternalApiServerException extends ExternalApiFallbackException {
    public ExternalApiServerException(String message) {
        super(message);
    }
}
