package site.radio.report.weekly.dto;

import java.time.LocalDateTime;

public interface WeeklyReportIdProjection {

    String getWeeklyReportId();

    LocalDateTime getLetterCreatedAt();
}
