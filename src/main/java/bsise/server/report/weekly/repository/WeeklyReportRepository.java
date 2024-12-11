package bsise.server.report.weekly.repository;

import bsise.server.report.monthly.dto.WeeklyReportMonthly;
import bsise.server.report.weekly.domain.WeeklyReport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, UUID> {

    @Query(value = """
            SELECT w.*
            FROM weekly_report w
            JOIN daily_report d ON w.weekly_report_id = d.weekly_report_id
            JOIN letter l ON d.daily_report_id = l.daily_report_id
            WHERE w.start_date = :startDate
              AND w.end_date = :endDate
              AND l.user_id = :userId
            """,
            nativeQuery = true)
    Optional<WeeklyReport> findDailyReportBy(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query(value = """
            SELECT w.week_of_year as weekOfYear, w.start_date as startDate, w.end_date as endDate
            FROM weekly_report w
            JOIN daily_report d ON w.weekly_report_id = d.weekly_report_id
            JOIN letter l ON d.daily_report_id = l.daily_report_id
            WHERE l.user_id = :userId AND w.end_date BETWEEN :startOfMonth AND :endOfMonth
            """, nativeQuery = true)
    List<WeeklyReportMonthly> findMonthlyWeeklyReports(UUID userId, LocalDateTime startOfMonth, LocalDateTime endOfMonth);
}
