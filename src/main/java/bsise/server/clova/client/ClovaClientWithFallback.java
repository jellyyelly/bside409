package bsise.server.clova.client;

import bsise.server.clova.dto.ClovaRequestDto;
import bsise.server.clova.dto.ClovaResponseDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClovaClientWithFallback implements ClovaFeignClient {

    @Override
    public ClovaResponseDto sendToClova(String apiKey, String apigwKey, String requestId,
                                        ClovaRequestDto clovaRequestDto) {
        log.error("fallback occurred.");
        return ClovaResponseDto.defaultFallbackResponse();
    }
}
