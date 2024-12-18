package bsise.server.clova.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClovaServiceFallbackFactory implements FallbackFactory<ClovaFeignClient> {

    @Override
    public ClovaFeignClient create(Throwable cause) {
        log.error("clova service fallback: ", cause);
        
        return new ClovaClientWithFallback();
    }
}
