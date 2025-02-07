package site.radio.reply.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.reply.domain.Letter;

@Repository
public interface LetterRepository extends JpaRepository<Letter, UUID> {

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
}
