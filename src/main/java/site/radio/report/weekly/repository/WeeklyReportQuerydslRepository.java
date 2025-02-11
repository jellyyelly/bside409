package site.radio.report.weekly.repository;

import java.time.LocalDate;
import java.util.UUID;
import site.radio.report.weekly.dto.WeeklyReportProjection;

public interface WeeklyReportQuerydslRepository {

    WeeklyReportProjection findWeeklyReportDtoBy(UUID userId, LocalDate startDate, LocalDate endDate);
}
