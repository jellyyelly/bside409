package site.radio.reply.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.radio.reply.domain.Letter;
import site.radio.reply.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, UUID>, ReplyQuerydslRepository {

    Optional<Reply> findByLetter(Letter letter);

    @Query("SELECT r FROM Reply r WHERE r.letter.published = TRUE ORDER BY r.createdAt DESC")
    List<Reply> findTopNReplies(Pageable pageable);

    /**
     * @deprecated 이 메서드는 더 이상 사용되지 않습니다.
     */
    @Deprecated
    @Query("SELECT r FROM Reply r where r.letter.user.id = :userId ORDER BY r.createdAt DESC")
    List<Reply> findTopNRepliesByUserId(UUID userId, Pageable pageable);
}
