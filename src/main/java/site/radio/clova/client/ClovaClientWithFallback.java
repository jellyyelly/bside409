package site.radio.clova.client;

import lombok.RequiredArgsConstructor;
import site.radio.clova.dto.ClovaRequestDto;
import site.radio.clova.dto.ClovaResponseDto;
import lombok.extern.slf4j.Slf4j;
import site.radio.error.ExternalApiFallbackException;

@Slf4j
@RequiredArgsConstructor
public class ClovaClientWithFallback implements ClovaFeignClient {

    private final Throwable cause;

    @Override
    public ClovaResponseDto sendToClova(String apiKey, String apigwKey, String requestId,
                                        ClovaRequestDto clovaRequestDto) {
        log.error("fallback occurred.");

        if (cause instanceof ExternalApiFallbackException apiFallbackException) {
            throw apiFallbackException;
        }

        return ClovaResponseDto.defaultFallbackResponse();
    }
}
