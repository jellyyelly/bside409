package site.radio.limiter;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
public class RateLimitController {

    private final RateLimitService rateLimitService;

    @GetMapping
    public RateLimitPolicyResponse getRateLimitPolicy() {
        return rateLimitService.retrieveCurrentPolicy();
    }
}
