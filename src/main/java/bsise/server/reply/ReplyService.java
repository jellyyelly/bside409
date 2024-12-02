package bsise.server.reply;

import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.clova.service.ClovaService;
import bsise.server.clova.dto.TwoTypeMessage;
import bsise.server.error.LetterNotFoundException;
import bsise.server.error.RateLimitException;
import bsise.server.error.UserNotFoundException;
import bsise.server.letter.*;
import bsise.server.limiter.RateLimitService;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReplyService {

    private final ClovaService clovaService;
    private final LetterService letterService;
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final ReplyRepository replyRepository;

    /**
     * <ol> 이 메서드는 순차대로 아래 작업을 수행합니다.
     * <li>유저가 작성한 편지를 기반으로 클로바로부터 답장을 생성합니다.</li>
     * <li>클로바가 생성한 답장을 유저가 작성한 편지와 연관짓고 저장합니다.</li>
     * </ol>
     *
     * @param letterDto 유저가 작성한 편지 정보가 저장되어있는 dto
     * @return 저장한 답장에 대한 응답 dto
     */
    public ReplyResponseDto makeAndSaveReply(LetterRequestDto letterDto) {
        if (!rateLimitService.isRequestAllowed(letterDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        User user = userRepository.findById(UUID.fromString(letterDto.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + letterDto.getUserId()));

        ClovaResponseDto clovaResponse = clovaService.send(letterDto.getMessage());
        TwoTypeMessage twoTypeMessage = clovaService.extract(clovaResponse);

        Letter letter = letterDto.toLetterWithoutUser();
        letter.setUser(user);

        Reply reply = Reply.builder()
                .letter(letterRepository.save(letter))
                .messageForF(twoTypeMessage.getMessageForF())
                .messageForT(twoTypeMessage.getMessageForT())
                .build();

        Reply savedReply = replyRepository.save(reply);

        return ReplyResponseDto.of(savedReply);
    }

    public ReplyResponseDto findReply(UUID letterId) {
        Letter letter = letterService.findLetter(letterId);
        Reply reply = replyRepository.findByLetter(letter)
                .orElseThrow(() -> new LetterNotFoundException("letter not found: " + letterId.toString()));

        return ReplyResponseDto.of(reply);
    }

    public List<ReplyResponseDto> findTopNLetterAndReply(Integer size) {
        size = correctSize(size);
        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNReplies(pageable);
        return replies.stream()
                .map(ReplyResponseDto::of)
                .toList();
    }

    public List<ReplyResponseDto> findMyLetterAndReply(UUID userId, Integer size) {
        validateUserId(userId);
        size = correctSize(size);

        PageRequest pageable = PageRequest.of(0, size, Sort.by(Direction.DESC, "createdAt"));
        List<Reply> replies = replyRepository.findTopNRepliesByUserId(userId, pageable);
        return replies.stream()
                .map(reply -> ReplyResponseDto.ofByUserId(reply, userId))
                .toList();
    }

    public Page<ReplyResponseDto> findMyLetterAndReply(UUID userId, Pageable pageable) {
        validateUserId(userId);

        Page<Reply> replies = replyRepository.findRepliesByOrderByCreatedAt(userId, pageable);
        List<ReplyResponseDto> replyDto = replies.stream()
                .map(reply -> ReplyResponseDto.ofByUserId(reply, userId))
                .toList();

        return new PageImpl<>(replyDto, pageable, replies.getTotalElements());
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
