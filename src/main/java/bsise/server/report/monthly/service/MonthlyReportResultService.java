package bsise.server.report.monthly.service;

import bsise.server.letter.LetterRepository;
import bsise.server.report.monthly.dto.MonthlyReportResultResponseDto;
import bsise.server.report.monthly.dto.MonthlyReportResultResponseDto.DailyReportStatusDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyReportResultService {

    private static final int DEFAULT_PREVIOUS_RANGE = 1;

    private final LetterRepository letterRepository;

    public MonthlyReportResultResponseDto getMonthlyReportResult(UUID userId, String yearMonthStr) {
        // 타겟 날짜(현재)로부터 한 달 전 날짜
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(DEFAULT_PREVIOUS_RANGE);

        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        return new MonthlyReportResultResponseDto(
                letterRepository
                        // 월간에 대해 날짜, 일일 대표 감정, letter 개수, weeklyReportId 조회
                        .findDailyReportStatusByDateRange(userId, startOfMonth, endOfMonth)
                        .stream()
                        .map(d -> DailyReportStatusDto.from(
                                d,
                                // available은 letter 개수가 > 0이고, 해당 날짜가 한달 전 날짜 이후고, dailyReportId가 null일 때만 true
                                d.getLetterCount() > 0
                                        && d.getDailyReportId() == null
                                        && !d.getDailyReportCreatedAt().toLocalDate().isBefore(oneMonthAgo)
                                        && !d.getDailyReportCreatedAt().toLocalDate().isAfter(now),
                                // analyzed는 dailyReportId가 null이 아니면 true
                                d.getDailyReportId() != null
                                )
                        )
                        .toList()
        );
    }
}
