package site.radio.error;

import feign.FeignException;
import feign.Response;

public class ExternalApiServerException extends FeignException.FeignServerException {

    public ExternalApiServerException(int status, String message, Response response, byte[] body) {
        super(status, message, response.request(), body, response.headers());
    }
}
