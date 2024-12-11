package bsise.server.common.cache;

import static bsise.server.common.cache.CacheGroup.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CacheScheduler {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private final CacheManager cacheManager;

    @Scheduled(cron = "0 0 0 * * *")
    public void clearCacheAtMidnight() {
        invalidate(DAILY_REPORT_STATUS.getCacheName(), DAILY_REPORT.getCacheName());
    }

    @Scheduled(cron = "0 0 0 * * Mon")
    public void clearCacheEveryMondayMidnight() {
        invalidate(WEEKLY_REPORT_STATUS.getCacheName(), WEEKLY_REPORT.getCacheName());
    }

    private void invalidate(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.invalidate();
            log.info("=== cleared cache: {} at {} ===", cacheName, ZonedDateTime.now(SEOUL));
        }
    }

    private void invalidate(String... cacheNames) {
        for (String cacheName : cacheNames) {
            invalidate(cacheName);
        }
    }
}
