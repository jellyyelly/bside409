package bsise.server.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LetterAnalysisRepository extends JpaRepository<LetterAnalysis, Long> {

    @Query(value = "SELECT la FROM LetterAnalysis la " +
            "JOIN Letter l ON la.letter.id = l.id " +
            "WHERE l.dailyReport.id = :dailyReportId")
    List<LetterAnalysis> findByDailyReportId(UUID dailyReportId);
}