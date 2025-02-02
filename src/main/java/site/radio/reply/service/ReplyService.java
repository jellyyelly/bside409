package site.radio.reply.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
import site.radio.reply.repository.ReplyRepository;
import site.radio.reply.dto.ReplyResponseDto;
import site.radio.reply.dto.TwoTypeMessage;
import site.radio.reply.dto.TwoTypeMessageExtractor;
import site.radio.reply.domain.Letter;
import site.radio.reply.domain.Reply;
import site.radio.reply.dto.ReplyResponse;
import site.radio.reply.dto.TwoTypeMessage;
import site.radio.reply.repository.ReplyRepository;
import site.radio.user.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyPromptTemplate replyPromptTemplate;
    private final ClovaService clovaService;
    private final LetterService letterService;
    private final UserRepository userRepository;
    private final ReplyRepository replyRepository;

    /**
     * <ol> 이 메서드는 순차대로 아래 작업을 수행합니다.
     * <li>유저가 저장한 편지를 기반으로 클로바로부터 답장을 생성합니다.</li>
     * <li>클로바가 생성한 답장을 유저가 작성한 편지와 연관짓고 저장합니다.</li>
     * </ol>
     *
     * @param letterResponse 유저의 편지 정보가 저장되어있는 dto
     * @return 저장한 답장에 대한 응답 dto
     */
    @CacheEvict(
            cacheNames = {"dailyReportStatus", "weeklyReportStatus"}, cacheManager = "caffeineCacheManager",
            key = "#letterResponse.userId.toString()"
    )
    public ReplyResponseDto makeAndSaveReply(LetterResponseDto letterResponse) {
        CreateResponse clovaResponse = clovaService.sendWithPromptTemplate(replyPromptTemplate,
                letterResponse.getContent());
        TwoTypeMessage twoTypeMessage = TwoTypeMessageExtractor.extract(clovaResponse.getResultMessage());

        Letter letter = letterService.findLetter(letterResponse.getLetterId());

        Reply reply = Reply.builder()
                .letter(letter)
                .messageForF(twoTypeMessage.getMessageForF())
                .messageForT(twoTypeMessage.getMessageForT())
                .build();

        Reply savedReply = replyRepository.save(reply);

        return ReplyResponseDto.of(savedReply);
    }

    public ReplyResponseDto save(Letter letter, TwoTypeMessage twoTypeMessage) {
        Reply reply = Reply.builder()
                .letter(letter)
                .messageForT(twoTypeMessage.getMessageForT())
                .messageForF(twoTypeMessage.getMessageForF())
                .build();

        Reply savedReply = replyRepository.save(reply);

        return ReplyResponseDto.of(savedReply);
    }

    @Transactional(readOnly = true)
    public ReplyResponseDto findReply(UUID letterId) {
        Letter letter = letterService.findLetter(letterId);
        Reply reply = replyRepository.findByLetter(letter)
                .orElseThrow(() -> new LetterNotFoundException("letter not found: " + letterId.toString()));

        return ReplyResponseDto.of(reply);
    }

    @Transactional(readOnly = true)
    public List<ReplyResponseDto> findTopNLetterAndReply(Integer size) {
        size = correctSize(size);
        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNReplies(pageable);
        return replies.stream()
                .map(ReplyResponseDto::of)
                .toList();
    }

    /**
     * @deprecated 이 메서드는 더 이상 사용되지 않습니다. `{@link Pageable}`을 인수로 받는 `{@code findMyLetterAndReply}`를 사용하세요.
     */
    @Deprecated
    public List<ReplyResponseDto> findMyLetterAndReply(UUID userId, Integer size) {
        validateUserId(userId);
        size = correctSize(size);

        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNRepliesByUserId(userId, pageable);
        return replies.stream()
                .map(reply -> ReplyResponseDto.ofByUserId(reply, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ReplyResponseDto> findMyLetterAndReply(UUID userId, int year, Boolean published, Pageable pageable) {
        validateUserId(userId);

        LocalDateTime startOfYear = Year.of(year).atDay(1).atStartOfDay();
        LocalDateTime endOfYear = Year.of(year).atMonth(12).atDay(31).atTime(LocalTime.MAX);

        Page<Reply> replies = replyRepository.findLatestRepliesBy(userId, startOfYear, endOfYear, published, pageable);

        return replies.map(reply -> ReplyResponseDto.ofByUserId(reply, userId));
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
