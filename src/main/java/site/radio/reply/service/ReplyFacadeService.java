package site.radio.reply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.radio.error.RateLimitException;
import site.radio.limiter.RateLimitService;
import site.radio.reply.dto.ReplyRequest;
import site.radio.reply.dto.ReplyResponse;
import site.radio.reply.dto.TwoTypeMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReplyFacadeService {

    private final RateLimitService rateLimitService;
    private final ReplyService replyService;

    public ReplyResponse responseReply(ReplyRequest replyRequest) {
        // 1. 사용 횟수 선차감
        if (!rateLimitService.preDeductUsage(replyRequest.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        try {
            // 2. 외부 API 호출
            TwoTypeMessage twoTypeMessage = replyService.sendLetterToClova(replyRequest);

            // 3. 트랜잭션 안에서 저장
            return replyService.save(replyRequest, twoTypeMessage);
        } catch (Exception e) {
            // 외부 API 장애 발생 시 사용 횟수 차감 롤백
            log.error("Clova API 호출 실패: {}", e.getMessage());
            rateLimitService.rollback(replyRequest.getUserId());
            throw e;
        }
    }
}
