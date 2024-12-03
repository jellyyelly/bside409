package bsise.server.reply;

import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.clova.dto.TwoTypeMessage;
import bsise.server.clova.service.ClovaService;
import bsise.server.error.RateLimitException;
import bsise.server.error.UserNotFoundException;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.letter.LetterRequestDto;
import bsise.server.limiter.RateLimitService;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterRollbackStrategy implements LetterReplyService {

    private final ClovaService clovaService;
    private final RateLimitService rateLimitService;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final ReplyRepository replyRepository;

    /**
     * <ol> 이 메서드는 순차대로 아래 작업을 수행합니다.
     * <li>유저가 작성한 편지를 기반으로 클로바로부터 답장을 생성합니다.</li>
     * <li>클로바가 생성한 답장을 유저가 작성한 편지와 연관짓고 저장합니다.</li>
     * <li>만약 답장 형식이 주어진 형식에서 벗어나면 요청이 실패하고 롤백됩니다.</li>
     * </ol>
     *
     * @param letterRequestDto 유저가 작성한 편지 정보가 저장되어있는 dto
     * @return 저장한 답장에 대한 응답 dto
     */
    @Override
    public ReplyResponseDto makeAndSaveReply(LetterRequestDto letterRequestDto) {
        if (!rateLimitService.isRequestAllowed(letterRequestDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        User user = userRepository.findById(UUID.fromString(letterRequestDto.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + letterRequestDto.getUserId()));

        ClovaResponseDto clovaResponse = clovaService.send(letterRequestDto.getMessage());
        TwoTypeMessage twoTypeMessage = clovaService.extract(clovaResponse);

        Letter letter = letterRequestDto.toLetterWithoutUser();
        letter.setUser(user);

        Reply reply = Reply.builder()
                .letter(letterRepository.save(letter))
                .messageForF(twoTypeMessage.getMessageForF())
                .messageForT(twoTypeMessage.getMessageForT())
                .build();

        Reply savedReply = replyRepository.save(reply);

        return ReplyResponseDto.of(savedReply);
    }
}
