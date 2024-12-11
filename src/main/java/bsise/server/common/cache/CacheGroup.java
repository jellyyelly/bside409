package bsise.server.common.cache;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CacheGroup {

    DAILY_REPORT_STATUS("dailyReportStatus", Duration.ofSeconds(24 * 60 * 60), 10_000, CacheType.LOCAL),
    WEEKLY_REPORT_STATUS("weeklyReportStatus", Duration.ofSeconds(7 * 24 * 60 * 60), 10_000, CacheType.LOCAL),
    DAILY_REPORT("dailyReport", Duration.ofSeconds(24 * 60 * 60), 10_000, CacheType.LOCAL),
    WEEKLY_REPORT("weeklyReport", Duration.ofSeconds(7 * 24 * 60 * 60), 10_000, CacheType.LOCAL),
    ;

    private final String cacheName;
    private final Duration expiredAfterWrite;
    private final long maximumSize;
    private final CacheType cacheType;
}
