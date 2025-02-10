package site.radio.reply.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.error.LetterNotFoundException;
import site.radio.error.UserNotFoundException;
import site.radio.reply.domain.Letter;
import site.radio.reply.domain.Reply;
import site.radio.reply.dto.ReplyRequest;
import site.radio.reply.dto.ReplyResponse;
import site.radio.reply.dto.TwoTypeMessage;
import site.radio.reply.dto.TwoTypeMessageExtractor;
import site.radio.reply.repository.ReplyRepository;
import site.radio.user.repository.UserRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyPromptTemplate prompt;
    private final ClovaService clovaService;
    private final LetterService letterService;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;

    public TwoTypeMessage sendLetterToClova(ReplyRequest replyRequest) {
        // 외부 API 호출
        CreateResponse clovaResponse = clovaService.sendWithPromptTemplate(prompt, replyRequest.getMessage());

        return TwoTypeMessageExtractor.extract(clovaResponse.getResultMessage());
    }

    @CacheEvict(
            cacheNames = {"dailyReportStatus", "weeklyReportStatus"}, cacheManager = "caffeineCacheManager",
            key = "#replyRequest.userId.toString()"
    )
    public ReplyResponse save(ReplyRequest replyRequest, TwoTypeMessage twoTypeMessage) {
        // 트랜잭션 안에서 Letter 저장
        Letter letter = letterService.save(
                UUID.fromString(replyRequest.getUserId()),
                replyRequest.getMessage(),
                replyRequest.getPreference(),
                replyRequest.isPublished());

        // Reply - Letter 연관 관계 매핑
        Reply reply = Reply.builder()
                .letter(letter)
                .messageForT(twoTypeMessage.getMessageForT())
                .messageForF(twoTypeMessage.getMessageForF())
                .build();

        return ReplyResponse.of(replyRepository.save(reply));
    }

    @Transactional(readOnly = true)
    public ReplyResponse findReply(UUID letterId) {
        Letter letter = letterService.findLetter(letterId);
        Reply reply = replyRepository.findByLetter(letter)
                .orElseThrow(() -> new LetterNotFoundException("letter not found: " + letterId.toString()));

        return ReplyResponse.of(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponse> findTopNLetterAndReply(Integer size) {
        size = correctSize(size);
        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNReplies(pageable);
        return replies.stream()
                .map(ReplyResponse::of)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReplyResponse> findMyLetterAndReply(UUID userId, int year, Boolean published, Pageable pageable) {
        validateUserId(userId);

        LocalDateTime startOfYear = Year.of(year).atDay(1).atStartOfDay();
        LocalDateTime endOfYear = Year.of(year).atMonth(12).atDay(31).atTime(LocalTime.MAX.minusNanos(999));

        Page<Reply> replies = replyRepository.findLatestRepliesBy(userId, startOfYear, endOfYear, published, pageable);

        return replies.map(reply -> ReplyResponse.ofByUserId(reply, userId));
    }

    private void validateUserId(UUID userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new UserNotFoundException("user not found");
        }
    }

    private Integer correctSize(Integer size) {
        if (size == null) {
            return 10;
        } else if (size > 10) {
            return 10;
        }
        return size;
    }
}
