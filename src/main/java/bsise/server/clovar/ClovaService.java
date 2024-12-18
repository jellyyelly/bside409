package bsise.server.clovar;

import static bsise.server.user.domain.Preference.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        ClovaResponseDto response = client.sendToClova(apiKey, apigwKey, requestId, ClovaRequestDto.from(message));

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
