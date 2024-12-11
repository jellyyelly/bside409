package bsise.server.report.monthly.dto;

import java.time.LocalDate;

public interface DailyReportMonthly {
    LocalDate getDate();
    String getDailyCoreEmotion();
}
