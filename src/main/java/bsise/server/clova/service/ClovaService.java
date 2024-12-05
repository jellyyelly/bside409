package bsise.server.clova.service;

import static bsise.server.user.domain.Preference.*;

import bsise.server.clova.dto.ClovaLetterReplyRequestDto;
import bsise.server.clova.dto.ClovaRequestDto;
import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.clova.dto.TwoTypeMessage;
import bsise.server.clova.client.ClovaFeignClient;
import bsise.server.clova.dailyReport.ClovaDailyReportRequestDto;
import bsise.server.clova.weekly.ClovaWeeklyReportRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("prod")
@Service
@RequiredArgsConstructor
public class ClovaService {

    @Value("${clova.api.key}")
    private String apiKey;

    @Value("${clova.apigw.key}")
    private String apigwKey;

    @Value("${clova.request.id}")
    private String requestId;

    private final ClovaFeignClient client;

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
        ClovaResponseDto response = client.sendToClova(apiKey, apigwKey, requestId, clovaRequestDto);

        if (response.hasErrorCode()) {
            throw new IllegalStateException("클로바 응답에 문제가 생겼습니다. 잠시 후 다시 시도하세요.");
        }

        return response;
    }

    public TwoTypeMessage extract(ClovaResponseDto response) {
        String messageF = MessageExtractor.extract(response.getResultMessage(), F);
        String messageT = MessageExtractor.extract(response.getResultMessage(), T);

        return TwoTypeMessage.of(messageF, messageT);
    }
}
