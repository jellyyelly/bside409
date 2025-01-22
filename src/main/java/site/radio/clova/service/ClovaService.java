package site.radio.clova.service;

import static site.radio.user.domain.Preference.F;
import static site.radio.user.domain.Preference.T;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import site.radio.clova.client.ClovaFeignClient;
import site.radio.clova.dailyReport.ClovaDailyReportRequestDto;
import site.radio.clova.dailyReport.DailyReportMessageParser;
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
import site.radio.letter.Letter;

@Profile({"prod", "dev"})
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(ClovaKeyProperties.class)
public class ClovaService {

    private final ClovaKeyProperties properties;
    private final Executor clovaExecutor;
    protected final ClovaFeignClient client;

    public ClovaResponseDto send(String message) {
        return sendRequestToClova(ClovaLetterReplyRequestDto.from(message));
    }

    public ClovaResponseDto sendDailyReportRequest(String message) {
        return sendRequestToClova(ClovaDailyReportRequestDto.from(message));
    }

    public ClovaResponseDto sendDailyReportRequest(List<Letter> letters) {
        String message = DailyReportMessageParser.requestClovaAnalysis(letters);
        return sendRequestToClova(ClovaDailyReportRequestDto.from(message));
    }

    public CompletableFuture<ClovaResponseDto> sendAsyncDailyReportRequest(List<Letter> letters) {
        return CompletableFuture.supplyAsync(() -> {
            String message = DailyReportMessageParser.requestClovaAnalysis(letters);
            return sendRequestToClova(ClovaDailyReportRequestDto.from(message));
        }, clovaExecutor);
    }

    public ClovaResponseDto sendWeeklyReportRequest(ClovaWeeklyReportRequestDto dto) {
        return sendRequestToClova(dto);
    }

    private ClovaResponseDto sendRequestToClova(ClovaRequestDto clovaRequestDto) {
        return client.sendToClova(
                properties.getApiKey(),
                properties.getApigwKey(),
                properties.getRequestId(),
                clovaRequestDto);
    }

    public TwoTypeMessage extract(ClovaResponseDto response) {
        String messageF = MessageExtractor.extract(response.getResultMessage(), F);
        String messageT = MessageExtractor.extract(response.getResultMessage(), T);

        return TwoTypeMessage.of(messageF, messageT);
    }
}
