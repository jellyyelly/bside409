package bsise.server.clova.client;

import bsise.server.clova.dto.ClovaRequestDto;
import bsise.server.clova.dto.ClovaResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "clova-service",
        url = "https://clovastudio.stream.ntruss.com",
        fallbackFactory = ClovaServiceFallbackFactory.class
)
public interface ClovaFeignClient {

    @PostMapping(
            value = "/testapp/v1/chat-completions/HCX-003",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE
    )
    ClovaResponseDto sendToClova(
            @RequestHeader("X-NCP-CLOVASTUDIO-API-KEY") String apiKey,
            @RequestHeader("X-NCP-APIGW-API-KEY") String apigwKey,
            @RequestHeader("X-NCP-CLOVASTUDIO-REQUEST-ID") String requestId,
            @RequestBody ClovaRequestDto clovaRequestDto
    );
}
