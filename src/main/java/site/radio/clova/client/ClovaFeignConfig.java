package site.radio.clova.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

@Slf4j
public class ClovaFeignConfig implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        int statusCode = response.status();
        String stringBody;
        try {
            byte[] byteArray = getResponseBodyByteArray(response);
            stringBody = new String(byteArray, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (400 <= statusCode && statusCode < 500) {
            log.error("[Status: {}] UpUpRadio 서버 --> 클로바 서버 호출 실패 [Body: {}]", statusCode, stringBody);
        }
        if (500 <= statusCode) {
            log.error("[Status: {}] 클로바 서버 오류 [Body: {}]", statusCode, stringBody);
        }
        ErrorDecoder errorDecoder = new Default();
        return errorDecoder.decode(methodKey, response);
    }

    private byte[] getResponseBodyByteArray(Response response) throws IOException {
        if (response.body() == null) {
            return new byte[]{};
        }
        return StreamUtils.copyToByteArray(response.body().asInputStream());
    }
}
