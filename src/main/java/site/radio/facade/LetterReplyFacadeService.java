package site.radio.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.ClovaResponseDto;
import site.radio.clova.letter.TwoTypeMessage;
import site.radio.clova.service.ClovaService;
import site.radio.error.RateLimitException;
import site.radio.letter.Letter;
import site.radio.letter.LetterRequestDto;
import site.radio.letter.LetterService;
import site.radio.limiter.RateLimitService;
import site.radio.reply.ReplyResponseDto;
import site.radio.reply.ReplyService;

@Slf4j
@RequiredArgsConstructor
@Service
public class LetterReplyFacadeService {

    private final RateLimitService rateLimitService;
    private final ClovaService clovaService;
    private final LetterService letterService;
    private final ReplyService replyService;

    public TwoTypeMessage sendLetterToClova(LetterRequestDto letterRequestDto) {
        // 사용 횟수 선차감
        if (!rateLimitService.preDeductUsage(letterRequestDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        // 외부 API 호출
        ClovaResponseDto clovaResponse = clovaService.send(letterRequestDto.getMessage());
        if (clovaResponse.hasFallbackMessage()) {
            // 외부 API 호출에 예외 발생 시 사용 횟수 롤백 이벤트 발행, event status => ROLLBACK_REQUIRED
            log.error("clova studio api 호출에 문제가 발생했습니다. userId: {}", letterRequestDto.getUserId());
            rateLimitService.rollback(letterRequestDto.getUserId());
        }
        return clovaService.extract(clovaResponse);
    }

    @Transactional
    public ReplyResponseDto responseReply(LetterRequestDto letterRequestDto, TwoTypeMessage twoTypeMessage) {
        Letter letter = letterService.save(
                UUID.fromString(letterRequestDto.getUserId()),
                letterRequestDto.getMessage(),
                letterRequestDto.getPreference(),
                letterRequestDto.isPublished());

        return replyService.save(letter, twoTypeMessage);
    }
}
