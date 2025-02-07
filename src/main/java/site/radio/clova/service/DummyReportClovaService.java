package site.radio.clova.service;

import java.util.concurrent.CompletableFuture;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import site.radio.clova.client.ClovaFeignClient;
import site.radio.clova.client.ClovaKeyProperties;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.prompt.PromptTemplate;
import site.radio.clova.report.ClovaWeeklyReportRequestDto;
import site.radio.report.daily.dto.DummyDailyReportClovaResponse;

/**
 * Clova API 를 사용하지 않고, 더미 데이터를 응답하는 더미 서비스입니다. 테스트 목적으로 생성되었으며, 운영 환경에서 사용할 수 없습니다.
 */
@Profile({"test"})
@Service
@EnableConfigurationProperties(ClovaKeyProperties.class)
public class DummyReportClovaService extends ClovaService {

    public DummyReportClovaService(ClovaFeignClient client) {
        super(client);
    }

    /**
     * 일반 편지를 생성할 때 사용하는 메서드를 대체합니다. 테스트 시 사용하지 않기 때문에 {@code null}을 반환하는 것을 유의해야 합니다.
     *
     * @param message 이 파라미터를 받아도 사용되지 않습니다.
     * @return {@code null}을 반환합니다.
     */
    @Override
    public CreateResponse send(String message) {
        return null;
    }

    @Override
    public CreateResponse sendWithPromptTemplate(PromptTemplate promptTemplate, String userMessage) {
        return super.sendWithPromptTemplate(promptTemplate, userMessage);
    }

    @Override
    public CompletableFuture<CreateResponse> sendAsyncWithPromptTemplate(PromptTemplate promptTemplate,
                                                                         String userMessage) {
        return super.sendAsyncWithPromptTemplate(promptTemplate, userMessage);
    }

    /**
     * message 를 구분자({@code ,})를 이용해 분리한 뒤, 편지 개수에 맞는 응답을 생성합니다. (최대 3개)
     *
     * @param message 편지의 내용을 구분자({@code ,})를 통해 하나로 만든 문자열
     * @return 더미 Clova 응답
     */
    @Override
    public CreateResponse sendDailyReportRequest(String message) {
        int lettersCount = message.split(message.split("\n", 2)[0]).length - 1;

        return DummyDailyReportClovaResponse.createDummy(lettersCount);
    }

    @Override
    public CreateResponse sendWeeklyReportRequest(ClovaWeeklyReportRequestDto dto) {
        return client.sendToClova("", "", "", dto);
    }
}
