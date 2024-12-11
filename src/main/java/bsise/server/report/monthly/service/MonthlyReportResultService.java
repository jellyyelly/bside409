package bsise.server.report.monthly.service;

import bsise.server.report.daily.repository.DailyReportRepository;
import bsise.server.report.monthly.dto.DailyReportMonthly;
import bsise.server.report.monthly.dto.MonthlyReportResultResponseDto;
import bsise.server.report.monthly.dto.WeeklyReportMonthly;
import bsise.server.report.weekly.repository.WeeklyReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyReportResultService {

    private final DailyReportRepository dailyReportRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    public MonthlyReportResultResponseDto getMonthlyReportResult(String userId, String yearMonthStr) {
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        UUID userUUID = UUID.fromString(userId);

        // 월간 날짜별 일일 대표 감정 조회 -> 날짜, 감정
        List<DailyReportMonthly> monthlyDailyReports = dailyReportRepository.findMonthlyDailyReports(userUUID, startOfMonth, endOfMonth);

        // 월간 주간 리포트 조회
        List<WeeklyReportMonthly> monthlyWeeklyReports = weeklyReportRepository.findMonthlyWeeklyReports(userUUID, startOfMonth, endOfMonth);

        return new MonthlyReportResultResponseDto(monthlyDailyReports, monthlyWeeklyReports);
    }
}
