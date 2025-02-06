package site.radio.reply.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.reply.domain.Letter;
import site.radio.report.retrieve.dto.DailyReportDto;
import site.radio.report.retrieve.dto.WeeklyReportDto;

@Repository
public interface LetterRepository extends JpaRepository<Letter, UUID> {

    Page<Letter> findLettersByUserId(UUID userId, Pageable pageable);

    List<Letter> findTop10ByPublishedIsTrueOrderByCreatedAtDesc();

    @Query(value = """
               SELECT l.*
               FROM letter l
               JOIN reply r ON l.letter_id = r.letter_id
               WHERE l.user_id = :userId AND l.created_at BETWEEN :startTime AND :endTime
               ORDER BY l.created_at DESC
               LIMIT 3
            """, nativeQuery = true)
    List<Letter> find3RecentLetters(UUID userId, LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = """
                SELECT
                    la.dailyReport.id AS dailyReportId,
                    d.coreEmotion AS coreEmotion,
                    la.createdAt AS createdAt
                FROM LetterAnalysis la
                LEFT JOIN la.dailyReport d
                WHERE la.letter.user.id = :userId
                    AND la.createdAt >= :startDate
                    AND la.createdAt <= :endDate
            """)
    List<DailyReportDto> findDailyReportIdByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    // TODO: 리팩토링 시 deprecated 예정
    @Query(value = """
                SELECT
                    d.weeklyReport.id AS weeklyReportId,
                    la.createdAt AS letterCreatedAt
                FROM LetterAnalysis la
                LEFT JOIN la.dailyReport d
                WHERE la.createdAt >= :startDate AND la.createdAt <= :endDate
                    AND la.letter.user.id = :userId
            """)
    List<WeeklyReportDto> findWeeklyReportIdByDateRange(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = """
                SELECT l
                FROM Letter l
                JOIN Reply r ON l.id = r.letter.id
                WHERE l.user.id = :userId AND
                      l.createdAt >= :start AND
                      l.createdAt <= :end
                ORDER BY l.createdAt DESC
            """)
    List<Letter> findByCreatedAtDesc(UUID userId, LocalDateTime start, LocalDateTime end);

    @Query(value = """
            SELECT letter_id, user_id, created_at, message, preference, published, like_f, like_t
            FROM (
              SELECT *, ROW_NUMBER() OVER (PARTITION BY DATE(created_at) ORDER BY created_at DESC) AS row_num
              FROM letter
              WHERE user_id = :userId
                AND created_at BETWEEN :startDate AND :endDate
                AND DATE(created_at) NOT IN (
                  SELECT DISTINCT DATE(l.created_at)
                  FROM letter l
                  JOIN letter_analysis la ON l.letter_id = la.letter_id
                  WHERE l.user_id = :userId
                    AND l.created_at BETWEEN :startDate AND :endDate
              )) AS analyzable_letters
            WHERE analyzable_letters.row_num <= 3
            ORDER BY created_at
            """, nativeQuery = true)
    List<Letter> findAnalyzableLetters(UUID userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            SELECT COUNT(CASE WHEN l.published = TRUE THEN 1 END) AS publishedCount,
                   COUNT(CASE WHEN l.published = FALSE THEN 1 END) AS unpublishedCount
            FROM Letter l
            JOIN l.user u
            WHERE l.user.id = :userId
                AND l.createdAt >= :startDate
                AND l.createdAt <= :endDate
            """)
    LetterStatistics fetchStatistics(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
}
