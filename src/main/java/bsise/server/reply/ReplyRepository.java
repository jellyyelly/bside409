package bsise.server.reply;

import bsise.server.letter.Letter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, UUID> {

    Optional<Reply> findByLetter(Letter letter);

    @Query("SELECT r FROM Reply r ORDER BY r.createdAt DESC")
    List<Reply> findTopNReplies(Pageable pageable);

    @Query("SELECT r FROM Reply r where r.letter.user.id = :userId ORDER BY r.createdAt DESC")
    List<Reply> findTopNRepliesByUserId(UUID userId, Pageable pageable);

    @Query("SELECT r FROM Reply r where r.letter.user.id = :userId ORDER BY r.createdAt DESC")
    Page<Reply> findRepliesByOrderByCreatedAt(UUID userId, Pageable pageable);
}
