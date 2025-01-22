package site.radio.limiter;

import static site.radio.limiter.RateLimitPolicy.LIMIT;
import static site.radio.limiter.RateLimitPolicy.TTL;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private static final String USER_KEY_REMAINING = "user:%s:remaining";

    @Value("${redis.limit}")
    private long BASIC_REQUEST_LIMIT;

    @Value("${redis.expire}")
    private long BASIC_EXPIRE_TIME;

    private final StringRedisTemplate redisTemplate;

    /**
     * 유저의 요청이 제한 횟수를 초과하는지 유저 아이디로 조회하는 메서드.
     *
     * @param userId {@link java.util.UUID} 의 String 타입
     * @return 제한 횟수 초과 여부
     */
    public boolean preDeductUsage(String userId) {
        String key = getKey(userId);

        // 요청 횟수 증가
        Long requestCount = redisTemplate.opsForValue().increment(key, 1);
        Assert.notNull(requestCount, String.format("회원의 요청 횟수가 null 입니다. userId: %s", userId));

        if (requestCount == 1) {
            // 최초 요청일 경우 만료시간 설정
            redisTemplate.expire(key, getCurrentPolicy(TTL), TimeUnit.SECONDS);
        }

        // 요청 횟수가 제한을 초과하면 false 반환
        return requestCount <= getCurrentPolicy(LIMIT);
    }

    public UserUsageResponseDto getUsageByUserId(String userId) {
        String key = getKey(userId);

        // 현재까지 사용량
        String usage = redisTemplate.opsForValue().get(key);

        // 기본 초 단위 / ttl이 없거나 만료: -1 / 존재하지 않는 키: -2
        Long expire = redisTemplate.getExpire(key);

        return UserUsageResponseDto.of(userId, usage, expire);
    }

    public RateLimitPolicyResponseDto retrieveCurrentPolicy() {
        Long limit = getCurrentPolicy(LIMIT);
        Long ttl = getCurrentPolicy(TTL);

        return RateLimitPolicyResponseDto.of(limit, ttl);
    }

    @Transactional
    @EventListener
    public void rollback(RateLimitRollbackEvent event) {
        // event status => ROLLBACK_PROCESSING
        event.process();

        String key = getKey(event.getUserId());
        redisTemplate.opsForValue().decrement(key, 1);
        log.info("사용 횟수가 롤백 되었습니다. userId: {}", event.getUserId());

        // event status => ROLLBACK_COMPLETE
        event.complete();
    }

    private String getKey(String userId) {
        return String.format(USER_KEY_REMAINING, userId);
    }

    private Long getCurrentPolicy(RateLimitPolicy policy) {
        String currentPolicy = redisTemplate.opsForValue().get(policy.getRedisKey());
        if (StringUtils.hasText(currentPolicy)) {
            return Long.parseLong(currentPolicy);
        }

        // 정책이 없는 경우 (null) 기본값 반환
        if (policy == TTL) {
            return BASIC_EXPIRE_TIME;
        }
        return BASIC_REQUEST_LIMIT;
    }
}
