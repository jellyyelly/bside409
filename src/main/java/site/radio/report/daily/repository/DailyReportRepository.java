package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportStaticsDto;
import site.radio.report.weekly.domain.WeeklyReport;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {

    @Query("""
            SELECT COUNT(d) > 0
            FROM DailyReport d
            JOIN Letter l ON d.id = l.dailyReport.id
            WHERE d.targetDate = :targetDate AND l.user.id = :userId
            """)
    boolean existsByUserAndTargetDate(UUID userId, LocalDate targetDate);

    @Query("""
            SELECT d
            FROM DailyReport d
            JOIN Letter l ON d.id = l.dailyReport.id
            WHERE d.targetDate = :targetDate AND l.user.id = :userId
            """)
    Optional<DailyReport> findByUserAndTargetDate(UUID userId, LocalDate targetDate);

    @Query("""
            SELECT d
            FROM DailyReport d
            JOIN Letter l ON d.id = l.dailyReport.id
            WHERE l.user.id = :userId AND d.targetDate IN :dates
            """)
    List<DailyReport> findByTargetDateIn(UUID userId, List<LocalDate> dates);

    @Query(value = """
            SELECT
                COUNT(CASE WHEN l.published = TRUE THEN 1 END) AS publishedCount,
                COUNT(CASE WHEN l.published = FALSE THEN 1 END) AS unPublishedCount
            FROM daily_report d
            JOIN letter l ON d.daily_report_id = l.daily_report_id
            WHERE l.user_id = :userId AND d.target_date IN :dateRange
            """, nativeQuery = true)
    DailyReportStaticsDto findStaticsBy(UUID userId, List<LocalDate> dateRange);

    @Query("""
            SELECT d
            FROM WeeklyReport w
            JOIN DailyReport d ON w.id = d.weeklyReport.id
            JOIN Letter l ON d.id = l.dailyReport.id AND l.user.id = :userId
            WHERE w.startDate = :startDate AND w.endDate = :endDate
            """)
    List<DailyReport> findDailyReportsWithWeeklyReport(UUID userId, LocalDate startDate, LocalDate endDate);

    @Query("""
            UPDATE DailyReport d
            SET d.weeklyReport = :weeklyReport
            WHERE d.id IN :dailyReportIds
            """)
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void updateWeeklyReportId(WeeklyReport weeklyReport, List<UUID> dailyReportIds);
}
