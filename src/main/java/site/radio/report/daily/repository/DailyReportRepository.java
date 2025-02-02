package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportStaticsDto;

@Repository
public interface DailyReportRepository extends JpaRepository<DailyReport, UUID> {

    @Query("""
            SELECT COUNT(*) > 0
            FROM LetterAnalysis la
            WHERE la.dailyReport.targetDate = :targetDate AND la.letter.user.id = :userId
            """)
    boolean existsByUserAndTargetDate(UUID userId, LocalDate targetDate);

    @Query("""
            SELECT d
            FROM LetterAnalysis la
            JOIN la.dailyReport d
            WHERE la.dailyReport.targetDate = :targetDate AND la.letter.user.id = :userId
            """)
    Optional<DailyReport> findByUserAndTargetDate(UUID userId, LocalDate targetDate);

    @Query("""
            SELECT d
            FROM LetterAnalysis la
            JOIN la.dailyReport d
            WHERE la.letter.user.id = :userId AND la.dailyReport.targetDate IN :dates
            """)
    List<DailyReport> findByTargetDateIn(UUID userId, List<LocalDate> dates);

    @Query(value = """
            SELECT
                COUNT(CASE WHEN la.letter.published = TRUE THEN 1 END) AS publishedCount,
                COUNT(CASE WHEN la.letter.published = FALSE THEN 1 END) AS unPublishedCount
            FROM LetterAnalysis la
            WHERE la.letter.user.id = :userId AND la.dailyReport.targetDate IN :dateRange
            """)
    DailyReportStaticsDto findStaticsBy(UUID userId, List<LocalDate> dateRange);

    @Query("""
            SELECT d
            FROM WeeklyReport w
            JOIN LetterAnalysis la ON la.dailyReport.weeklyReport.id = w.id AND la.letter.user.id = :userId
            JOIN DailyReport d ON la.dailyReport.id = d.id
            WHERE w.startDate = :startDate AND w.endDate = :endDate
            """)
    List<DailyReport> findDailyReportsWithWeeklyReport(UUID userId, LocalDate startDate, LocalDate endDate);
}
