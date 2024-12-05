package bsise.server.report.daily.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import bsise.server.auth.OAuth2Provider;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.report.daily.domain.DailyReport;
import bsise.server.report.daily.dto.DailyReportDto.CreateRequest;
import bsise.server.report.daily.dto.DailyReportResponseDto;
import bsise.server.report.daily.repository.DailyReportRepository;
import bsise.server.user.domain.Preference;
import bsise.server.user.domain.Role;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = "clova.msg.separator=test-sep")
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

    @DisplayName("외부 API 연동을 대체하는 더미 응답으로 데일리 리포트를 생성할 수 있다.")
    @Test
    @Transactional
    void createDummyDailyReport() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        Letter letter1 = createLetterByLocalDate(user, localDate);
        Letter letter2 = createLetterByLocalDate(user, localDate);

        CreateRequest dto = CreateRequest.builder()
                .userId(user.getId().toString())
                .date(localDate)
                .build();

        // when
        DailyReportResponseDto dailyReport = dailyReportService.createDailyReport(dto);

        // then
        assertThat(dailyReport.getDate()).isEqualTo(localDate);
    }

    @DisplayName("데일리 리포트 생성 요청이 동시에 여러 번 오더라도 한 번만 생성된다.")
    @Test
    void testConcurrentDailyReportCreation() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        Letter letter = createLetterByLocalDate(user, localDate);

        CreateRequest dto = CreateRequest.builder()
                .userId(user.getId().toString())
                .date(localDate)
                .build();

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                startLatch.await();
                dailyReportService.createDailyReport(dto); // 데일리 리포트 생성 요청
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

        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(List.of(localDate));

        assertAll(
                "데일리 리포트는 하나만 생성된다.",
                () -> assertThat(exceptions).hasSize(threadCount - 1),
                () -> assertThat(deadlockCount).isEqualTo(0L),
                () -> assertThat(dailyReports).hasSize(1)
        );
    }

    @DisplayName("데일리 리포트 생성 요청이 동시에 여러 번 오더라도 한 번만 생성된다.")
    @Test
    void testConcurrentDailyReportCreationWithFacade() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();
        LocalDate localDate = LocalDate.of(2024, 10, 1);
        Letter letter = createLetterByLocalDate(user, localDate);

        CreateRequest dto = CreateRequest.builder()
                .userId(user.getId().toString())
                .date(localDate)
                .build();

        int threadCount = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                startLatch.await();
                namedLockDailyReportFacade.createDailyReportWithNamedLock(dto); // 데일리 리포트 생성 요청
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

        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(List.of(localDate));

        assertAll(
                "데일리 리포트는 하나만 생성된다.",
                () -> assertThat(exceptions).hasSize(threadCount - 1),
                () -> assertThat(deadlockCount).isEqualTo(0L),
                () -> assertThat(dailyReports).hasSize(1)
        );
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

        letterRepository.saveAndFlush(letter);

        return letter;
    }
}