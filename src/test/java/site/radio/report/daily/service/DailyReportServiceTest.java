package site.radio.report.daily.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;
import site.radio.auth.OAuth2Provider;
import site.radio.common.cache.CacheGroup;
import site.radio.reply.domain.Letter;
import site.radio.reply.repository.LetterRepository;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportResponseDto;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.user.domain.Preference;
import site.radio.user.domain.Role;
import site.radio.user.domain.User;
import site.radio.user.repository.UserRepository;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class DailyReportServiceTest {

    @Autowired
    private DailyReportService dailyReportService;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private NamedLockDailyReportFacade namedLockDailyReportFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private CacheManager cacheManager;

    @DisplayName("외부 API 연동을 대체하는 더미 응답으로 데일리 리포트를 생성할 수 있다.")
    @Test
    @Transactional
    void createDummyDailyReport() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        createLetterByLocalDate(user, localDate);
        createLetterByLocalDate(user, localDate);

        // when
        DailyReportResponseDto dailyReport = dailyReportService.createDailyReport(user.getId(), localDate);

        // then
        assertThat(dailyReport.getDate()).isEqualTo(localDate);
    }

    @DisplayName("데일리 리포트 생성 요청이 동시에 여러 번 오더라도 한 번만 생성된다.")
    @Test
    void testConcurrentDailyReportCreation() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        createLetterByLocalDate(user, localDate);

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                startLatch.await();
                dailyReportService.createDailyReport(user.getId(), localDate); // 데일리 리포트 생성 요청
            } catch (Throwable t) {
                exceptions.add(t);
            } finally {
                endLatch.countDown();
            }
        };

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);

        // then
        executorService.shutdown();
        long deadlockCount = exceptions.stream()
                .filter(exception -> exception.getMessage().contains("Deadlock"))
                .count();

        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(user.getId(), List.of(localDate));

        assertAll(
                "데일리 리포트는 하나만 생성된다.",
                () -> assertThat(exceptions).hasSize(threadCount - 1),
                () -> assertThat(deadlockCount).isEqualTo(0L),
                () -> assertThat(dailyReports).hasSize(1)
        );
    }

    @DisplayName("데일리 리포트 생성 요청이 동시에 여러 번 오더라도 한 번만 생성된다.")
    @Test
    void testConcurrentDailyReportCreationWithFacade()
            throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        createLetterByLocalDate(user, localDate);

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                startLatch.await();
                namedLockDailyReportFacade.createDailyReportWithNamedLock(user.getId(), localDate); // 데일리 리포트 생성 요청
            } catch (Throwable t) {
                exceptions.add(t);
            } finally {
                endLatch.countDown();
            }
        };

        // when
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(task);
        }

        startLatch.countDown();
        endLatch.await(60, TimeUnit.SECONDS);

        // then
        executorService.shutdown();
        long deadlockCount = exceptions.stream()
                .filter(exception -> exception.getMessage().contains("Deadlock"))
                .count();

        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(user.getId(), List.of(localDate));

        assertAll(
                "데일리 리포트는 하나만 생성된다.",
                () -> assertThat(exceptions).hasSize(threadCount - 1),
                () -> assertThat(deadlockCount).isEqualTo(0L),
                () -> assertThat(dailyReports).hasSize(1)
        );
    }

    @DisplayName("한 명의 유저가 같은 날에 데일리 리포트 조회를 5번 호출하면 4번 캐시 히트된다")
    @Test
    void should_cache_state_when_called_multiple_times() throws Exception {
        // given
        User user = createTestUser();
        LocalDate targetDate = LocalDate.of(2024, 10, 1);
        createLetterByLocalDate(user, targetDate);

        // when
        for (int i = 0; i < 5; i++) {
            dailyReportService.getDailyReport(user.getId(), targetDate);
        }

        // then
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CacheGroup.DAILY_REPORT.getCacheName());
        assert caffeineCache != null;
        Cache<Object, Object> cache = caffeineCache.getNativeCache();
        CacheStats cacheStats = cache.stats();

        assertThat(cache.estimatedSize()).isEqualTo(1L);
        assertThat(cacheStats.missCount()).isEqualTo(1L);
        assertThat(cacheStats.hitCount()).isEqualTo(4L);
        assertThat(cacheStats.evictionCount()).isEqualTo(0L);
    }

    private User createTestUser() {
        User user = User.builder()
                .username("tester1")
                .nickname("tester1")
                .email("test")
                .preference(Preference.F)
                .provider(OAuth2Provider.KAKAO)
                .role(Role.OAUTH)
                .build();

        return userRepository.save(user);
    }

    private @NotNull Letter createLetterByLocalDate(User user, LocalDate localDate)
            throws NoSuchFieldException, IllegalAccessException {
        Letter letter = Letter.builder()
                .user(user)
                .message("testMsg")
                .build();

        letterRepository.save(letter);

        // superClass => getDeclaredField() 사용
        Field createdAt = Letter.class.getSuperclass().getDeclaredField("createdAt");
        createdAt.setAccessible(true);
        createdAt.set(letter, LocalDateTime.of(localDate, LocalTime.MIN));

        return letterRepository.saveAndFlush(letter);
    }
}