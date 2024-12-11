package bsise.server.report.monthly.dto;

import java.time.LocalDate;

public interface WeeklyReportMonthly {
    int getWeekOfYear();
    LocalDate getStartDate();
    LocalDate getEndDate();
}
