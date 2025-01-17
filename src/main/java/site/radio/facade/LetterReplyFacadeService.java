package site.radio.facade;

import feign.FeignException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.ClovaResponseDto;
import site.radio.clova.letter.TwoTypeMessage;
import site.radio.clova.service.ClovaService;
import site.radio.error.RateLimitException;
import site.radio.letter.LetterCreationEvent;
import site.radio.letter.LetterRequestDto;
import site.radio.limiter.RateLimitRollbackEvent;
import site.radio.limiter.RateLimitService;
import site.radio.reply.ReplyResponseDto;

@Slf4j
@RequiredArgsConstructor
@Service
public class LetterReplyFacadeService {

    private final RateLimitService rateLimitService;
    private final ClovaService clovaService;
    private final ApplicationEventPublisher eventPublisher;

    public TwoTypeMessage sendLetterToClova(LetterRequestDto letterRequestDto) {
        // 사용 횟수 선차감
        if (!rateLimitService.preDeductUsage(letterRequestDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }

        try {
            // 외부 API 호출
            ClovaResponseDto clovaResponse = clovaService.send(letterRequestDto.getMessage());
            return clovaService.extract(clovaResponse);
        } catch (FeignException e) {
            // 외부 API 호출에 예외 발생 시 사용 횟수 롤백 이벤트 발행, event status => ROLLBACK_REQUIRED
            log.error("clova studio api 호출에 문제가 발생했습니다. userId: {}", letterRequestDto.getUserId());
            eventPublisher.publishEvent(RateLimitRollbackEvent.createEvent(letterRequestDto.getUserId()));
            throw e;
        }
    }

    @Transactional
    public ReplyResponseDto responseReply(LetterRequestDto letterRequestDto, TwoTypeMessage twoTypeMessage) {
        CompletableFuture<ReplyResponseDto> future = new CompletableFuture<>();

        // event status => PENDING
        LetterCreationEvent event = LetterCreationEvent.createEvent(
                letterRequestDto.getUserId(),
                letterRequestDto.getMessage(),
                letterRequestDto.getPreference(),
                letterRequestDto.isPublished(),
                twoTypeMessage,
                future);

        // 편지 생성 이벤트 발행
        eventPublisher.publishEvent(event);

        try {
            // 편지부터 답장까지 생성이 완료되면 응답을 반환
            return future.get(120, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException("Failed future for reply response", e);
        }
    }
}
