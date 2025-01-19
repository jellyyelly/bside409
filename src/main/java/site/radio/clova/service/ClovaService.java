package site.radio.clova.service;

import static site.radio.user.domain.Preference.F;
import static site.radio.user.domain.Preference.T;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import site.radio.clova.client.ClovaFeignClient;
import site.radio.clova.dailyReport.ClovaDailyReportRequestDto;
import site.radio.clova.dto.ClovaRequestDto;
import site.radio.clova.dto.ClovaResponseDto;
import site.radio.clova.letter.ClovaLetterReplyRequestDto;
import site.radio.clova.letter.MessageExtractor;
import site.radio.clova.letter.TwoTypeMessage;
import site.radio.clova.weekly.ClovaWeeklyReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.radio.error.ExternalApiUnhandledException;

@Slf4j
@Profile({"prod", "dev"})
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(ClovaKeyProperties.class)
public class ClovaService {

    private final ClovaKeyProperties properties;
    protected final ClovaFeignClient client;

    public ClovaResponseDto send(String message) {
        return sendRequestToClova(ClovaLetterReplyRequestDto.from(message));
    }

    public ClovaResponseDto sendDailyReportRequest(String message) {
        return sendRequestToClova(ClovaDailyReportRequestDto.from(message));
    }

    public ClovaResponseDto sendWeeklyReportRequest(ClovaWeeklyReportRequestDto dto) {
        return sendRequestToClova(dto);
    }

    private ClovaResponseDto sendRequestToClova(ClovaRequestDto clovaRequestDto) {
        try {
            return client.sendToClova(
                    properties.getApiKey(),
                    properties.getApigwKey(),
                    properties.getRequestId(),
                    clovaRequestDto);
        } catch (NoFallbackAvailableException e) {
            Throwable cause = e.getCause();

            if(cause instanceof FeignException feignException) {
                throw feignException;
            } else {
                throw new ExternalApiUnhandledException("알 수 없는 이유로 외부 api 호출에 실패했습니다.", cause);
            }
        }
    }

    public TwoTypeMessage extract(ClovaResponseDto response) {
        String messageF = MessageExtractor.extract(response.getResultMessage(), F);
        String messageT = MessageExtractor.extract(response.getResultMessage(), T);

        return TwoTypeMessage.of(messageF, messageT);
    }
}
