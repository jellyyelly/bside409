package bsise.server.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Profile({"dev", "test"})
@RequiredArgsConstructor
@RestController("/circuit")
public class CircuitBreakerTestController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/close")
    public ResponseEntity<Void> close(@RequestParam String name) {
        circuitBreakerRegistry.circuitBreaker(name)
                .transitionToClosedState();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/open")
    public ResponseEntity<Void> open(@RequestParam String name) {
        circuitBreakerRegistry.circuitBreaker(name)
                .transitionToOpenState();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<CircuitBreaker.State> status(@RequestParam String name) {
        CircuitBreaker.State state = circuitBreakerRegistry.circuitBreaker(name)
                .getState();

        return ResponseEntity.ok(state);
    }

    @GetMapping("/all")
    public ResponseEntity<Void> all() {
        Set<CircuitBreaker> circuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers();
        for (CircuitBreaker circuitBreaker : circuitBreakers) {
            log.error("circuitName={}, state={}", circuitBreaker.getName(), circuitBreaker.getState());
        }
        return ResponseEntity.ok().build();
    }
}
