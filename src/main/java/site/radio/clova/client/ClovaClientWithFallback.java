package site.radio.clova.client;

import lombok.extern.slf4j.Slf4j;
import site.radio.clova.dto.CreateRequest;
import site.radio.clova.dto.CreateResponse;

@Slf4j
public class ClovaClientWithFallback implements ClovaFeignClient {

    @Override
    public CreateResponse sendToClova(CreateRequest createRequestDto) {
        log.error("fallback occurred.");
        return CreateResponse.defaultFallbackResponse();
    }
}
