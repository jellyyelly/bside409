package site.radio.report.daily.repository;

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
}
