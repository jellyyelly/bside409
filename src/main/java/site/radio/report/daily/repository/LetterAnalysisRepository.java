package site.radio.report.daily.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.daily.domain.LetterAnalysis;

@Repository
public interface LetterAnalysisRepository extends JpaRepository<LetterAnalysis, Long> {

    @Query("""
            SELECT la FROM LetterAnalysis la
            WHERE la.dailyReport.id = :dailyReportId
            """)
    List<LetterAnalysis> findByDailyReportId(UUID dailyReportId);

    @Query("""
            SELECT la
            FROM LetterAnalysis la
            JOIN FETCH la.dailyReport d
            WHERE la.letter.user.id = :userId
                AND la.dailyReport.targetDate = :targetDate
            """)
    List<LetterAnalysis> findLetterAnalysesByTargetDateAt(UUID userId, LocalDate targetDate);

    @Query("""
            SELECT la
            FROM LetterAnalysis la
            JOIN FETCH la.dailyReport d
            JOIN FETCH la.letter l
            WHERE la.letter.user.id = :userId
                AND la.dailyReport.targetDate BETWEEN :startDate AND :endDate
            """)
    List<LetterAnalysis> findLetterAnalysesByDateRangeIn(UUID userId, LocalDate startDate, LocalDate endDate);
}
