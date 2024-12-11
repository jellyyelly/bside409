package bsise.server.report.monthly.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class MonthlyReportResultResponseDto {

    private final List<DailyReportMonthly> dailyReports;
    private final List<WeeklyReportMonthly> weeklyReports;
}
