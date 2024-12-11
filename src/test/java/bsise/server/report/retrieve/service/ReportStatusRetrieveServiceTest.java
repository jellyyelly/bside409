package bsise.server.report.retrieve.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bsise.server.auth.OAuth2Provider;
import bsise.server.common.cache.CacheGroup;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.report.daily.domain.CoreEmotion;
import bsise.server.report.daily.domain.DailyReport;
import bsise.server.report.weekly.domain.WeeklyReport;
import bsise.server.report.daily.repository.DailyReportRepository;
import bsise.server.report.retrieve.dto.DailyReportStatusResponseDto;
import bsise.server.report.retrieve.dto.WeeklyReportStatusResponseDto;
import bsise.server.report.weekly.repository.WeeklyReportRepository;
import bsise.server.user.domain.Preference;
import bsise.server.user.domain.Role;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class ReportStatusRetrieveServiceTest {

    @Autowired
    private ReportStatusRetrieveService reportStatusRetrieveService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private CacheManager cacheManager;

    @Transactional
    @Test
    void findDailyAnalysisStatus() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();

        // 2024-11-15 1건(일일 분석 0건), 2024-11-30 2건(일일 분석 1건), 2024-12-29 2건(일일 분석 0건), 2024-12-30 1건(일일 분석 0건)
        createLetterScenarios1(user);

        // when & then
        verifyFirstCase(user);
        verifySecondCase(user);
        verifyThirdCase(user);
    }

    // 2024-11-15 1건(분석 수행 0건), 2024-11-30 2건(분석 수행 1건), 2024-12-29 2건(분석 수행 0건), 2024-12-30 1건(분석 수행 0건)
    private void createLetterScenarios1(User user)
            throws NoSuchFieldException, IllegalAccessException {
        // 2024-11-15: 1건
        createLetterByLocalDate(user, LocalDate.of(2024, 11, 15));

        /* 2024-11-30: 전체 2건 */
        createLetterByLocalDate(user, LocalDate.of(2024, 11, 30));

        // 2024-11-30: 일일 분석된 편지 1건
        Letter analyzedLetter = createLetterByLocalDate(user, LocalDate.of(2024, 11, 30));
        DailyReport dailyReport = createTestDailyReport(LocalDate.of(2024, 11, 30));
        analyzedLetter.setDailyReport(dailyReport);

        // 2024-12-29: 2건
        createLetterByLocalDate(user, LocalDate.of(2024, 12, 29));
        createLetterByLocalDate(user, LocalDate.of(2024, 12, 29));

        // 2024-12-30: 1건
        createLetterByLocalDate(user, LocalDate.of(2024, 12, 30));
    }



    private void verifyFirstCase(User user) {
        LocalDate targetDate = LocalDate.of(2024, 11, 30);
        List<DailyReportStatusResponseDto> dailyReportStatus = reportStatusRetrieveService.findDailyReportStatus(user.getId(), targetDate, targetDate);

        List<LocalDate> totalDates = targetDate.minusMonths(1).datesUntil(targetDate.plusDays(1)).toList();

        assertAll(
                "이미 분석을 실행한 2024-11-30 조회 시 2024-11-15 1건만 분석 가능하다.",
                () -> assertThat(dailyReportStatus).hasSize(32),
                () -> assertThat(dailyReportStatus).extracting(DailyReportStatusResponseDto::getDate).isEqualTo(totalDates),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 11, 15)))
                        .extracting(DailyReportStatusResponseDto::isAvailable)
                        .containsExactly(true),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 11, 15)))
                        .extracting(DailyReportStatusResponseDto::isAnalyzed)
                        .containsExactly(false)
        );
    }

    private void verifySecondCase(User user) {
        LocalDate targetDate = LocalDate.of(2024, 12, 29);
        List<DailyReportStatusResponseDto> dailyReportStatus = reportStatusRetrieveService.findDailyReportStatus(user.getId(), targetDate, targetDate);

        List<LocalDate> totalDates = targetDate.minusMonths(1).datesUntil(targetDate.plusDays(1)).toList();

        assertAll(
                "한 번도 분석하지 않은 2024-12-29 조회 시 2024-12-29 에 작성한 글 2건만 분석 가능하다.",
                () -> assertThat(dailyReportStatus).hasSize(31),
                () -> assertThat(dailyReportStatus).extracting(DailyReportStatusResponseDto::getDate).isEqualTo(totalDates),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 29)))
                        .extracting(DailyReportStatusResponseDto::isAvailable)
                        .containsExactly(true),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 29)))
                        .extracting(DailyReportStatusResponseDto::isAnalyzed)
                        .containsExactly(false)
        );
    }

    private void verifyThirdCase(User user) {
        LocalDate targetDate = LocalDate.of(2024, 12, 30);
        List<DailyReportStatusResponseDto> dailyReportStatus = reportStatusRetrieveService.findDailyReportStatus(user.getId(), targetDate, targetDate);

        List<LocalDate> totalDates = targetDate.minusMonths(1).datesUntil(targetDate.plusDays(1)).toList();

        assertAll(
                "한 번도 분석하지 않은 2024-12-30 조회 시 2024-12-29 작성 글 2건, 2024-12-30 작성 글 1건에 대해 분석 가능하다.",
                () -> assertThat(dailyReportStatus).hasSize(31),
                () -> assertThat(dailyReportStatus).extracting(DailyReportStatusResponseDto::getDate).isEqualTo(totalDates),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 29)))
                        .extracting(DailyReportStatusResponseDto::isAvailable)
                        .containsExactly(true),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 30)))
                        .extracting(DailyReportStatusResponseDto::isAvailable)
                        .containsExactly(true),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 29)))
                        .extracting(DailyReportStatusResponseDto::isAnalyzed)
                        .containsExactly(false),
                () -> assertThat(dailyReportStatus)
                        .filteredOn(dto -> dto.getDate().isEqual(LocalDate.of(2024, 12, 30)))
                        .extracting(DailyReportStatusResponseDto::isAnalyzed)
                        .containsExactly(false)
        );
    }

    @Transactional
    @Test
    void findWeeklyReportStatus() throws NoSuchFieldException, IllegalAccessException {
        // given
        User user = createTestUser();

        // 2024-11-30 (일일 분석 1, 주간 분석 1), 2024-12-29 (일일 분석 0, 주간 분석 0), 2024-12-30 (일일 분석 1, 주간 분석 0)
        createLetterScenarios2(user);

        // when
        /* 이미 주간 분석을 실행한 2024-11-30 날짜에 조회 시 */
        LocalDate targetDate = LocalDate.of(2024, 11, 30);
        List<WeeklyReportStatusResponseDto> weeklyReportStatus = reportStatusRetrieveService.findWeeklyReportStatus(user.getId(), targetDate, targetDate);

        /* 일일 분석과 주간 분석을 하지않은 2024-12-29 날짜일 때 조회 시  */
        LocalDate targetDate2 = LocalDate.of(2024, 12, 29);
        List<WeeklyReportStatusResponseDto> weeklyReportStatus2 = reportStatusRetrieveService.findWeeklyReportStatus(user.getId(), targetDate2, targetDate2);

        /* 주간 분석을 하지않은 2024-12-30 날짜일 때 조회 시  */
        LocalDate targetDate3 = LocalDate.of(2024, 12, 30);
        List<WeeklyReportStatusResponseDto> weeklyReportStatus3 = reportStatusRetrieveService.findWeeklyReportStatus(user.getId(), targetDate3, targetDate3);

        // then
        assertAll(
                "2024-11-30일에 이미 주간 분석을 실행한 주에 조회 시 주간 분석이 가능한 주 개수는 0건이다.",
                () -> assertThat(weeklyReportStatus).hasSize(5),
                () -> assertThat(weeklyReportStatus).filteredOn(WeeklyReportStatusResponseDto::isAvailable)
                        .extracting(WeeklyReportStatusResponseDto::getWeekName).isEmpty()
        );

        assertAll(
                "2024-12-29일에 조회 시 주간 분석이 가능한 주 개수는 1건이다.",
                () -> assertThat(weeklyReportStatus2).hasSize(5),
                () -> assertThat(weeklyReportStatus2).filteredOn(WeeklyReportStatusResponseDto::isAvailable)
                        .extracting(WeeklyReportStatusResponseDto::getWeekName).containsExactly("2024년 12월 4주차")
        );

        assertAll(
                "2024-12-30일에 조회 시 주간 분석이 가능한 주 개수는 2건이다.",
                () -> assertThat(weeklyReportStatus3).hasSize(6),
                () -> assertThat(weeklyReportStatus3).filteredOn(WeeklyReportStatusResponseDto::isAvailable)
                        .extracting(WeeklyReportStatusResponseDto::getWeekName).contains("2024년 12월 4주차", "2025년 1월 1주차")
        );
    }

    private void createLetterScenarios2(User user)
            throws NoSuchFieldException, IllegalAccessException {
        /* 2024-11-30: 편지 1건 (일일 분석 수행 o, 주간 분석 수행 o) */
        Letter analyzedLetter = createLetterByLocalDate(user, LocalDate.of(2024, 11, 30));
        DailyReport dailyReport = createTestDailyReport(LocalDate.of(2024, 11, 30));
        analyzedLetter.setDailyReport(dailyReport);

        WeeklyReport weeklyReport = WeeklyReport.builder()
                .cheerUp("test")
                .publishedCount(0)
                .unpublishedCount(0)
                .build();
        weeklyReportRepository.save(weeklyReport);

        dailyReport.setWeeklyReport(weeklyReport);
        dailyReportRepository.save(dailyReport);

        /* 2024-12-29: 편지 1건 (일일 분석 수행 x, 주간 분석 수행 x) */
        createLetterByLocalDate(user, LocalDate.of(2024, 12, 29));

        /* 2024-12-30: 편지 1건 (일일 분석 수행 o, 주간 분석 수행 x) */
        Letter analyzedLetter3 = createLetterByLocalDate(user, LocalDate.of(2024, 12, 30));
        DailyReport dailyReport3 = createTestDailyReport(LocalDate.of(2024, 12, 29));
        analyzedLetter3.setDailyReport(dailyReport3);
    }

    private DailyReport createTestDailyReport(LocalDate targetDate) {
        DailyReport dailyReport = DailyReport.builder()
                .coreEmotion(CoreEmotion.기쁨)
                .description("test")
                .targetDate(targetDate)
                .build();

        return dailyReportRepository.save(dailyReport);
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
                .build();

        letterRepository.save(letter);

        // superClass => getDeclaredField() 사용
        Field createdAt = Letter.class.getSuperclass().getDeclaredField("createdAt");
        createdAt.setAccessible(true);
        createdAt.set(letter, LocalDateTime.of(localDate, LocalTime.MIN));

        letterRepository.saveAndFlush(letter);

        return letter;
    }

    @DisplayName("날짜가 주어지면 해당 날짜가 속한 주의 시작 일자와 마지막 일자를 계산할 수 있다")
    @ParameterizedTest
    @MethodSource("provideTargetDate")
    void inclusiveRange(LocalDate targetDate, LocalDate expectedStartDate) {
        // given
        LocalDate startDate = targetDate.minusMonths(1);

        // when
        List<LocalDate> dates = startDate.datesUntil(targetDate.plusDays(1)).toList();

        // then
        LocalDate actualStartDate = dates.get(0);
        LocalDate lastDate = dates.get(dates.size() - 1);

        assertThat(actualStartDate).isEqualTo(expectedStartDate);
        assertThat(lastDate).isEqualTo(targetDate);
    }

    private static Stream<Arguments> provideTargetDate() {
        return Stream.of(
                Arguments.of(
                        LocalDate.of(2024, 11, 15),
                        LocalDate.of(2024, 10, 15)
                ),
                Arguments.of(
                        LocalDate.of(2024, 11, 30),
                        LocalDate.of(2024, 10, 30)
                ),
                Arguments.of(
                        LocalDate.of(2024, 12, 1),
                        LocalDate.of(2024, 11, 1)
                ),
                Arguments.of(
                        LocalDate.of(2024, 12, 15),
                        LocalDate.of(2024, 11, 15)
                ),
                Arguments.of(
                        LocalDate.of(2024, 12, 31),
                        LocalDate.of(2024, 11, 30)
                ),
                Arguments.of(
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2024, 12, 1)
                )
        );
    }

    @DisplayName("특정 날짜가 주어지면 한국(ISO) 기준으로 weekOfYear 를 계산할 수 있다")
    @ParameterizedTest
    @MethodSource("provideTargetDateAndWeekOfYear")
    void weekOfWeekByKoreaWeekFields(LocalDate targetDate, int expectedWeekOfYear) {
        // given
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 4);

        // when
        int weekOfYear = targetDate.get(weekFields.weekOfWeekBasedYear());

        // then
        assertThat(weekOfYear).isEqualTo(expectedWeekOfYear);
    }

    public static Stream<Arguments> provideTargetDateAndWeekOfYear() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 12, 29), 52),
                Arguments.of(LocalDate.of(2024, 12, 31), 1),
                Arguments.of(LocalDate.of(2025, 1, 1), 1),
                Arguments.of(LocalDate.of(2025, 12, 28), 52),
                Arguments.of(LocalDate.of(2025, 12, 31), 1),
                Arguments.of(LocalDate.of(2026, 1, 1), 1),
                Arguments.of(LocalDate.of(2026, 1, 5), 2),
                Arguments.of(LocalDate.of(2026, 12, 27), 52),
                Arguments.of(LocalDate.of(2026, 12, 31), 53),
                Arguments.of(LocalDate.of(2027, 1, 1), 53),
                Arguments.of(LocalDate.of(2027, 1, 4), 1)
        );
    }

    @DisplayName("targetDate 에 따른 해당 주의 시작 일자와 마지막 일자를 계산할 수 있다")
    @ParameterizedTest
    @MethodSource("provideTargetDateAndExpectedDates")
    void findFirstDayOfWeekAndLastDayOfWeek(LocalDate targetDate, LocalDate expectedStartDate,
                                            LocalDate expectedEndDate) {
        // when
        LocalDate startDateByISO = targetDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDateByISO = targetDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // ISO => 동일한 결과
        LocalDate startDateByKorea = targetDate.with(WeekFields.ISO.getFirstDayOfWeek());
        LocalDate endDateByKorea = startDateByKorea.plusDays(6);

        // then
        assertThat(startDateByISO).isEqualTo(expectedStartDate);
        assertThat(endDateByISO).isEqualTo(expectedEndDate);
        assertThat(startDateByKorea).isEqualTo(expectedStartDate);
        assertThat(endDateByKorea).isEqualTo(expectedEndDate);
    }

    private static Stream<Arguments> provideTargetDateAndExpectedDates() {
        return Stream.of(
                Arguments.of(
                        LocalDate.of(2024, 11, 28),
                        LocalDate.of(2024, 11, 25),
                        LocalDate.of(2024, 12, 1)
                ),
                Arguments.of(
                        LocalDate.of(2024, 12, 29),
                        LocalDate.of(2024, 12, 23),
                        LocalDate.of(2024, 12, 29)
                ),
                Arguments.of(
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2024, 12, 30),
                        LocalDate.of(2025, 1, 5)
                )
        );
    }

    @DisplayName("주어진 날짜로부터 한국 기준(ISO) weekOfYear 와 weekOfMonth 를 `yyyy년 m월 n주차`로 계산할 수 있다")
    @ParameterizedTest
    @MethodSource("provideTargetDateAndExpectedWeekOfYear")
    void test(LocalDate targetDate, int expectedWeekOfYear, String expectedDayOfMonth) {
        // given
        WeekFields korea = WeekFields.of(DayOfWeek.MONDAY, 4);

        // when
        int weekOfYearByISO = targetDate.get(WeekFields.ISO.weekOfWeekBasedYear());
        int weekOfYearByKorea = targetDate.get(korea.weekOfWeekBasedYear());

        String weekOfMonth = CustomDateUtils.getWeekOfMonth(targetDate);

        // then
        assertThat(weekOfYearByISO).isEqualTo(expectedWeekOfYear);
        assertThat(weekOfYearByKorea).isEqualTo(expectedWeekOfYear);
        assertThat(weekOfMonth).isEqualTo(expectedDayOfMonth);
    }

    private static Stream<Arguments> provideTargetDateAndExpectedWeekOfYear() {
        return Stream.of(
                Arguments.of(LocalDate.of(2024, 11, 28), 48, "2024년 11월 4주차"),
                Arguments.of(LocalDate.of(2024, 12, 1), 48, "2024년 11월 4주차"),
                Arguments.of(LocalDate.of(2024, 12, 3), 49, "2024년 12월 1주차"),
                Arguments.of(LocalDate.of(2024, 12, 23), 52, "2024년 12월 4주차"),
                Arguments.of(LocalDate.of(2024, 12, 29), 52, "2024년 12월 4주차"),
                Arguments.of(LocalDate.of(2024, 12, 31), 1, "2025년 1월 1주차"),
                Arguments.of(LocalDate.of(2025, 1, 1), 1, "2025년 1월 1주차"),
                Arguments.of(LocalDate.of(2025, 12, 31), 1, "2026년 1월 1주차"),
                Arguments.of(LocalDate.of(2026, 1, 1), 1, "2026년 1월 1주차")
        );
    }

    @DisplayName("한 명의 유저가 같은 날에 데일리 리포트 상태 조회를 5번 호출하면 4번 캐시 히트된다")
    @Test
    void should_cache_state_when_called_multiple_times() throws Exception {
        // given
        User user = createTestUser();
        LocalDate targetDate = LocalDate.of(2024, 11, 30);
        createLetterByLocalDate(user, targetDate);

        // when
        for (int i = 0; i < 5; i++) {
            reportStatusRetrieveService.findDailyReportStatus(user.getId(), targetDate, targetDate);
        }

        // then
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache(CacheGroup.DAILY_REPORT_STATUS.getCacheName());
        Cache<Object, Object> cache = caffeineCache.getNativeCache();
        CacheStats cacheStats = cache.stats();

        assertThat(cache.estimatedSize()).isEqualTo(1L);
        assertThat(cacheStats.missCount()).isEqualTo(1L);
        assertThat(cacheStats.hitCount()).isEqualTo(4L);
        assertThat(cacheStats.evictionCount()).isEqualTo(0L);
    }
}