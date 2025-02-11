package site.radio.clova.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import site.radio.clova.dto.CreateRequest;
import site.radio.clova.dto.CreateResponse;

@FeignClient(
        name = "clova-service",
        url = "${feign.clova.url}",
        configuration = ClovaFeignConfig.class,
        fallbackFactory = ClovaServiceFallbackFactory.class
)
public interface ClovaFeignClient {

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    CreateResponse sendToClova(@RequestBody CreateRequest createRequest);
}
