package site.radio.report.weekly.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.report.weekly.domain.WeeklyReport;

@Repository
public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, UUID>, WeeklyReportQuerydslRepository {

    @Query(value = """
            SELECT 1
            FROM weekly_report w
            JOIN daily_report d ON w.weekly_report_id = d.weekly_report_id
            JOIN letter_analysis la ON d.daily_report_id = la.daily_report_id
            JOIN letter l ON la.letter_id = l.letter_id AND l.user_id = :userId
            WHERE w.start_date = :startDate AND w.end_date = :endDate
            LIMIT 1
            """, nativeQuery = true)
    Optional<Integer> fetchCountBy(UUID userId, LocalDate startDate, LocalDate endDate);
}
