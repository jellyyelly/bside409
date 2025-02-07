package site.radio.report.retrieve.dto;

import java.time.LocalDateTime;

public interface WeeklyReportIdProjection {

    String getWeeklyReportId();

    LocalDateTime getLetterCreatedAt();
}
