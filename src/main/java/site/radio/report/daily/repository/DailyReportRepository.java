package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportIdProjection;
import site.radio.report.weekly.domain.WeeklyReport;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {

    @Query("""
            SELECT COUNT(*) > 0
            FROM LetterAnalysis la
            WHERE la.dailyReport.targetDate = :targetDate AND la.letter.user.id = :userId
            """)
    boolean existsByUserAndTargetDate(UUID userId, LocalDate targetDate);

    @Modifying
    @Query("UPDATE DailyReport d SET d.weeklyReport = :weeklyReport WHERE d IN :dailyReportIds")
    void bulkUpdateWeeklyReport(WeeklyReport weeklyReport, List<UUID> dailyReportIds);

    @Query("""
            SELECT
                d.id AS dailyReportId,
                d.coreEmotion AS coreEmotion,
                l.createdAt AS letterCreatedAt
            FROM Letter l
            LEFT JOIN LetterAnalysis la ON l.id = la.letter.id
            LEFT JOIN la.dailyReport d ON la.dailyReport.id = d.id
            WHERE l.user.id = :userId
                AND l.createdAt BETWEEN :startDate AND :endDate
            """)
    List<DailyReportIdProjection> findDailyReportIdByDateRange(UUID userId, LocalDateTime startDate,
                                                               LocalDateTime endDate);
}
