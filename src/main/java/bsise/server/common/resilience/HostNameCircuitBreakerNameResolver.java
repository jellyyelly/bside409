package bsise.server.common.resilience;

import feign.Target;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HostNameCircuitBreakerNameResolver implements CircuitBreakerNameResolver {

    @Override
    public String resolveCircuitBreakerName(String feignClientName, Target<?> target, Method method) {
        String url = target.url();

        try {
            return new URL(url).getHost();
        } catch (MalformedURLException e) {
            log.error("MalformedURLException => {}", url);
            return "default";
        }
    }
}
