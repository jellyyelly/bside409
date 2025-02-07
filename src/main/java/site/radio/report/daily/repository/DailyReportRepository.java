package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.DailyReport;
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
}
