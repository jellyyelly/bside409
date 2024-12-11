package bsise.server.report.retrieve.service;

import static java.util.stream.Collectors.groupingBy;

import bsise.server.letter.LetterRepository;
import bsise.server.report.retrieve.dto.DailyReportDto;
import bsise.server.report.retrieve.dto.DailyReportStatusResponseDto;
import bsise.server.report.retrieve.dto.WeeklyReportDto;
import bsise.server.report.retrieve.dto.WeeklyReportStatusResponseDto;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReportStatusRetrieveService {

    private static final WeekFields WEEK_FIELDS = WeekFields.of(DayOfWeek.MONDAY, 4);
    private static final int DEFAULT_PREVIOUS_RANGE = 1;

    private final LetterRepository letterRepository;

    /**
     * 유저의 ID(uuid)를 통해 일자 별 일일 분석 리포트 상태 조회
     *
     * @param userId     유저의 아이디
     * @param targetDate 대상 날짜
     * @param endDate    검색 범위 마지막 날짜
     * @return 일자 별 일일 분석 리포트 상태 리스트
     */
    @Cacheable(
            cacheNames = "dailyReportStatus", cacheManager = "caffeineCacheManager",
            key = "#userId.toString() + #targetDate.toString()", unless = "#result == null || #result.size() == 0"
    )
    public List<DailyReportStatusResponseDto> findDailyReportStatus(UUID userId, LocalDate targetDate,
                                                                    LocalDate endDate) {
        // 타겟 날짜로부터 한 달 전 날짜
        LocalDate oneMonthAgo = targetDate.minusMonths(DEFAULT_PREVIOUS_RANGE);

        // 편지 리스트 조회
        List<DailyReportDto> letters = letterRepository.findDailyReportIdByDateRange(userId, convertToMin(oneMonthAgo),
                convertToMax(endDate));

        // LocalDate 로 변환 후 날짜 기준으로 그루핑
        Map<LocalDate, List<DailyReportDto>> lettersByDate = letters.stream()
                .collect(groupingBy(letter -> letter.getCreatedAt().toLocalDate()));

        // 한 달 이전부터 타겟 날짜를 포함한 모든 일자 구하기
        List<LocalDate> totalDateRange = getInclusiveDateRange(oneMonthAgo, targetDate);

        // 해당 날짜에 작성한 편지가 있으면 분석 상태 표시 없으면 항상 분석 불가능한 상태로 응답
        return totalDateRange.stream()
                .map(date -> lettersByDate.containsKey(date)
                        ? DailyReportStatusResponseDto.create(date, lettersByDate.get(date))
                        : DailyReportStatusResponseDto.createFalseStatus(date))
                .toList();
    }

    /**
     * 유저의 ID(uuid)를 통해 주간 별 주간 분석 리포트 상태 조회
     *
     * @param userId     유저의 아이디
     * @param targetDate 대상 날짜
     * @param endDate    검색 범위 마지막 날짜
     * @return 주간 별 주간 분석 리포트 상태 리스트
     */
    @Cacheable(
            cacheNames = "weeklyReportStatus", cacheManager = "caffeineCacheManager",
            key = "#userId.toString() + #targetDate.toString()", unless = "#result == null || #result.size() == 0"
    )
    public List<WeeklyReportStatusResponseDto> findWeeklyReportStatus(UUID userId, LocalDate targetDate,
                                                                      LocalDate endDate) {
        // 타겟 날짜로부터 한 달 전 날짜
        LocalDate oneMonthAgo = targetDate.minusMonths(DEFAULT_PREVIOUS_RANGE);

        // 편지 리스트 조회
        List<WeeklyReportDto> dailyReports = letterRepository.findWeeklyReportIdByDateRange(userId,
                convertToMin(oneMonthAgo), convertToMax(endDate));

        // 편지 작성일을 weekOfYear 로 변환 후 주간 기준으로 그루핑
        Map<Integer, List<WeeklyReportDto>> reportsByWeekOfYear = dailyReports.stream()
                .collect(groupingBy(report -> report.getLetterCreatedAt().get(WEEK_FIELDS.weekOfWeekBasedYear())));

        // 한 달 이전부터 타겟 날짜를 포함한 주간별 일자 구하기
        Map<Integer, List<LocalDate>> totalWeekRange = getInclusiveWeekRange(oneMonthAgo, targetDate);

        return totalWeekRange.entrySet().stream()
                .map(entry -> {
                    Integer weekOfYear = entry.getKey();
                    List<LocalDate> datesByWeek = entry.getValue();
                    return reportsByWeekOfYear.containsKey(weekOfYear)
                            ? WeeklyReportStatusResponseDto.create(weekOfYear, datesByWeek,
                            reportsByWeekOfYear.get(weekOfYear))
                            : WeeklyReportStatusResponseDto.createFalseStatus(weekOfYear, datesByWeek);
                })
                .toList();
    }

    private LocalDateTime convertToMin(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MIN);
    }

    private LocalDateTime convertToMax(LocalDate localDate) {
        return LocalDateTime.of(localDate, LocalTime.MAX);
    }

    /**
     * 주어진 날짜 범위로부터 1달 전까지의 일자 별 달력 생성 (마지막 일자 포함)
     *
     * @param startDate (inclusive) 범위의 시작일
     * @param endDate   (inclusive) 범위의 종료일
     * @return 시작일부터 종료일을 모두 포함하는 날짜(LocalDate) (이른 날짜부터 정렬된 상태)
     */
    private List<LocalDate> getInclusiveDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }

    /**
     * 주어진 날짜 범위로부터 1달 전까지의 주간 별 달력 생성 (마지막 일자 포함)
     *
     * @param startDate (inclusive) 범위의 시작일
     * @param endDate   (inclusive) 범위의 종료일
     * @return 시작일부터 종료일을 모두 포함하는 주간 별로 그룹화된 날짜 (weekOfYear 를 기준으로 이른 날짜부터 정렬된 상태)
     */
    private Map<Integer, List<LocalDate>> getInclusiveWeekRange(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.groupingBy(
                        date -> date.get(WEEK_FIELDS.weekOfWeekBasedYear()),
                        TreeMap::new,
                        Collectors.toList()
                ));
    }
}
