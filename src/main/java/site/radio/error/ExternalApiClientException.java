package site.radio.error;

public class ExternalApiClientException extends ExternalApiFallbackException {
    public ExternalApiClientException(String message) {
        super(message);
    }
}
