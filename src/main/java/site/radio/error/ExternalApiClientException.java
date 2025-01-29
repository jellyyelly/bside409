package site.radio.error;

import feign.FeignException;
import feign.Response;

public class ExternalApiClientException extends FeignException.FeignClientException {
    public ExternalApiClientException(int status, String message, Response response, byte[] body) {
        super(status, message, response.request(), body, response.headers());
    }
}
