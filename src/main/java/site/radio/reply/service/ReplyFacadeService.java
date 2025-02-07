package site.radio.reply.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.error.RateLimitException;
import site.radio.limiter.RateLimitService;
import site.radio.reply.domain.Letter;
import site.radio.reply.dto.ReplyRequest;
import site.radio.reply.dto.ReplyResponse;
import site.radio.reply.dto.TwoTypeMessage;
import site.radio.reply.dto.TwoTypeMessageExtractor;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReplyFacadeService {

    private final ReplyPromptTemplate prompt;
    private final RateLimitService rateLimitService;
    private final ClovaService clovaService;
    private final LetterService letterService;
    private final ReplyService replyService;

    public TwoTypeMessage sendLetterToClova(ReplyRequest replyRequest) {
        // 사용 횟수 선차감
        if (!rateLimitService.preDeductUsage(replyRequest.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        // 외부 API 호출
        CreateResponse clovaResponse = clovaService.sendWithPromptTemplate(prompt, replyRequest.getMessage());

        TwoTypeMessage twoTypeMessage = null;
        try {
            twoTypeMessage = TwoTypeMessageExtractor.extract(clovaResponse.getResultMessage());
        } catch (IllegalArgumentException e) {
            log.error("clova studio api 호출에 문제가 발생했습니다. userId: {}", replyRequest.getUserId());
            rateLimitService.rollback(replyRequest.getUserId());
        }
        return twoTypeMessage;
    }

    @Transactional
    public ReplyResponse responseReply(ReplyRequest replyRequest, TwoTypeMessage twoTypeMessage) {
        Letter letter = letterService.save(
                UUID.fromString(replyRequest.getUserId()),
                replyRequest.getMessage(),
                replyRequest.getPreference(),
                replyRequest.isPublished());

        return replyService.save(letter, twoTypeMessage);
    }
}
