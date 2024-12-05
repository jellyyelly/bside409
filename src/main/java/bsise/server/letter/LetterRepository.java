package bsise.server.letter;

import bsise.server.report.retrieve.dto.DailyReportDto;
import bsise.server.report.retrieve.dto.WeeklyReportDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LetterRepository extends JpaRepository<Letter, UUID> {

    Page<Letter> findLettersByUserId(UUID userId, Pageable pageable);
    List<Letter> findTop10ByPublishedIsTrueOrderByCreatedAtDesc();

    @Query(value = """
               SELECT *
               FROM letter
               WHERE user_id = :userId AND created_at BETWEEN :startTime AND :endTime
               ORDER BY created_at DESC
               LIMIT 3
            """,
            nativeQuery = true)
    List<Letter> find3RecentLetters(UUID userId, LocalDateTime startTime, LocalDateTime endTime);

    @Query(
            value = """
                SELECT
                    daily_report_id AS dailyReportId,
                    created_at AS createdAt
                FROM letter
                WHERE user_id = :userId
                    AND created_at >= :startDate
                    AND created_at <= :endDate
            """,
            nativeQuery = true
    )
    List<DailyReportDto> findDailyReportIdByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(
            value = """
                SELECT
                    d.weekly_report_id AS weeklyReportId,
                    l.created_at AS letterCreatedAt
                FROM letter l
                LEFT JOIN daily_report d ON d.daily_report_id = l.daily_report_id
                WHERE l.created_at >= :startDate AND l.created_at <= :endDate
                    AND l.user_id = :userId
            """,
            nativeQuery = true
    )
    List<WeeklyReportDto> findWeeklyReportIdByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(
            value = """
                SELECT l
                FROM Letter l
                WHERE l.user.id = :userId AND
                      l.createdAt >= :start AND
                      l.createdAt < :end
                ORDER BY l.createdAt DESC
            """)
    List<Letter> findByCreatedAtDesc(UUID userId, LocalDateTime start, LocalDateTime end);
}
