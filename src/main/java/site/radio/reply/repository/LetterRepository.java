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

    // FIXME: 리팩토링 떄 사용할 예정
    @Query(value = """
                WITH report_dates AS ( /* 데일리 리포트가 생성된 날짜들 */
                    SELECT DISTINCT DATE(l2.created_at) AS report_date
                    FROM letter l2
                    WHERE l2.user_id = :userId
                      AND l2.daily_report_id IS NOT NULL
                      AND l2.created_at >= :startDate
                      AND l2.created_at < :endDate
                ),
                ranked_letters AS ( /* 데일리 리포트가 생성된 날짜와 left join 후 데일리 리포트 생성일이 없는 편지들을 최신순으로 선택 */
                    SELECT l1.letter_id, l1.created_at, l1.like_f, l1.like_t, l1.message, l1.preference, l1.published,
                        l1.daily_report_id, l1.user_id,
                        ROW_NUMBER() OVER (PARTITION BY DATE(l1.created_at) ORDER BY l1.created_at DESC) AS seq
                    FROM letter l1
                    LEFT JOIN report_dates dr ON DATE(l1.created_at) = dr.report_date
                    WHERE l1.user_id = :userId
                        AND dr.report_date IS NULL -- 데일리 리포트 생성일이 없는 경우
                        AND l1.created_at >= :startDate
                        AND l1.created_at <= :endDate
                )
                /* 최신순으로 정렬된 ranked_letters 에서 최신 3개까지만 선택 */
                SELECT letter_id, created_at, like_f, like_t, message, preference, published, daily_report_id, user_id
                FROM ranked_letters
                WHERE seq <= 3
            """, nativeQuery = true)
    List<Letter> findLettersForDailyReportCreation(UUID userId, LocalDateTime startDate, LocalDateTime endDate);
}
