package bsise.server.reply;

import static org.assertj.core.api.Assertions.assertThat;

import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ReplyRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private ReplyRepository replyRepository;


    @DisplayName("공개 여부 조건에 해당하는 편지의 답장을 가져올 수 있다")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void findRepliesByOrderByCreatedAt(boolean published) {
        // given
        User savedUser = userRepository.save(User.makeGuest());
        Letter savedLetter = letterRepository.save(makeLetter(savedUser, published));
        Reply savedReply = replyRepository.save(makeReply(savedLetter));

        LocalDateTime start = LocalDateTime.now().with(TemporalAdjusters.firstDayOfYear());
        LocalDateTime end = LocalDateTime.now().with(TemporalAdjusters.lastDayOfYear());

        // when
        Page<Reply> replies = replyRepository.findRepliesByOrderByCreatedAt(
                savedUser.getId(), start, end, published, Pageable.unpaged());

        Reply findReply = replies.getContent().get(0);

        // then
        assertThat(findReply).isEqualTo(savedReply);
        assertThat(findReply.getLetter().isPublished()).isEqualTo(published);
    }

    private Letter makeLetter(User user, boolean published) {
        return Letter.builder()
                .user(user)
                .published(published)
                .build();
    }

    private Reply makeReply(Letter letter) {
        return Reply.builder()
                .letter(letter)
                .build();
    }
}