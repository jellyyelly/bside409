package bsise.server.report.daily.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DailyReportStatus {
    LocalDate getReportDate();
    int getLetterCount();
    String getDailyReportId();
    String getWeeklyReportId();
    String getDailyCoreEmotion();
    LocalDateTime getDailyReportCreatedAt();
}
