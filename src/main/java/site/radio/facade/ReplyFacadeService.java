package site.radio.facade;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.error.RateLimitException;
import site.radio.letter.Letter;
import site.radio.letter.LetterRequest;
import site.radio.letter.LetterService;
import site.radio.limiter.RateLimitService;
import site.radio.reply.ReplyPromptTemplate;
import site.radio.reply.ReplyResponseDto;
import site.radio.reply.ReplyService;
import site.radio.reply.TwoTypeMessage;
import site.radio.reply.TwoTypeMessageExtractor;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReplyFacadeService {

    private final ReplyPromptTemplate prompt;
    private final RateLimitService rateLimitService;
    private final ClovaService clovaService;
    private final LetterService letterService;
    private final ReplyService replyService;

    public TwoTypeMessage sendLetterToClova(LetterRequest letterRequest) {
        // 사용 횟수 선차감
        if (!rateLimitService.preDeductUsage(letterRequest.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        // 외부 API 호출
        CreateResponse clovaResponse = clovaService.sendWithPromptTemplate(prompt, letterRequest.getMessage());

        TwoTypeMessage twoTypeMessage = null;
        try {
            twoTypeMessage = TwoTypeMessageExtractor.extract(clovaResponse.getResultMessage());
        } catch (IllegalArgumentException e) {
            log.error("clova studio api 호출에 문제가 발생했습니다. userId: {}", letterRequest.getUserId());
            rateLimitService.rollback(letterRequest.getUserId());
        }
        return twoTypeMessage;
    }

    @Transactional
    public ReplyResponseDto responseReply(LetterRequest letterRequest, TwoTypeMessage twoTypeMessage) {
        Letter letter = letterService.save(
                UUID.fromString(letterRequest.getUserId()),
                letterRequest.getMessage(),
                letterRequest.getPreference(),
                letterRequest.isPublished());

        return replyService.save(letter, twoTypeMessage);
    }
}
