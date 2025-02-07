package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportStatics;

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
    DailyReportStatics findStaticsBy(UUID userId, List<LocalDate> dateRange);

}
