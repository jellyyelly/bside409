package bsise.server.report.retrieve.dto;

import java.time.LocalDateTime;

public interface DailyReportDto {

    String getDailyReportId();
    LocalDateTime getCreatedAt();
}