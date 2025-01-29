package site.radio.clova.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import site.radio.clova.dto.ClovaResponseDto;
import site.radio.clova.service.DummyReportClovaService;
import site.radio.clova.weekly.ClovaWeeklyReportRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import site.radio.error.ExternalApiClientException;
import site.radio.error.ExternalApiServerException;

@ActiveProfiles("test")
@SpringBootTest
@TestPropertySource(properties = "feign.clova.url=http://localhost:${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
class ClovaFeignClientTest {

    @Autowired
    private DummyReportClovaService clovaService;

    @DisplayName("WireMock을 사용하여 클로바 서버를 모킹하고, FeignClient 의 로그를 확인하는데 사용하세요")
    @Test
    void logging() {
        // given, WireMock을 static import
        stubFor(post("/")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":{\"code\":\"20000\",\"message\":\"OK\"},\"result\":{\"message\":{\"role\":\"assistant\",\"content\":\"일상 속에서 크고 작은 문제들로 인해 스트레스를 받고 계시는군요. 많은 고민거리들로 인해 머릿속이 복잡하시겠지만 이럴 때일수록 심호흡을 하고 마음을 가라앉혀 보는 건 어떠세요? 모든 문제를 한 번에 해결할 수는 없으니 우선순위를 정해 하나씩 처리해 나가는 것도 좋은 방법이랍니다. 그러다 보면 어느 순간 상황이 조금씩 나아지고 있음을 느낄 수 있을 거예요.사용자님께서는 진로에 대한 고민과 더불어 학업 및 인간관계에서도 어려움을 겪고 계시는군요. 이런 상황에서는 누구나 불안하고 지칠 수 있어요. 그럼에도 포기하지 않고 꾸준히 노력하시는 모습이 정말 대단하다고 생각해요. 힘들 때는 잠시 쉬어가며 자신을 돌보고, 주변 사람들에게 도움을 요청하는 것도 좋은 방법이니 참고해 보시길 바라요.\"},\"inputLength\":1203,\"outputLength\":166,\"stopReason\":\"stop_before\",\"seed\":3923155011,\"aiFilter\":[{\"groupName\":\"curse\",\"name\":\"insult\",\"score\":\"2\",\"result\":\"OK\"},{\"groupName\":\"curse\",\"name\":\"discrimination\",\"score\":\"2\",\"result\":\"OK\"},{\"groupName\":\"unsafeContents\",\"name\":\"sexualHarassment\",\"score\":\"2\",\"result\":\"OK\"}]}}")));

        // when
        ClovaWeeklyReportRequestDto dto = ClovaWeeklyReportRequestDto.from("테스트용");
        ClovaResponseDto result = clovaService.sendWeeklyReportRequest(dto);

        // then
        assertThat(result).isExactlyInstanceOf(ClovaResponseDto.class);
    }

    @DisplayName("클로바 서버 요청 시 FeignClientException이 발생하면 ExternalApiClientException이 throw 된다.")
    @Test
    void checkClovaClientException() {
        // given
        stubFor(post("/")
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("clova server 400 error test body")
                )
        );

        // when & then
        assertThatThrownBy(() -> clovaService.sendWeeklyReportRequest(ClovaWeeklyReportRequestDto.from("테스트용")))
                .isExactlyInstanceOf(ExternalApiClientException.class);
    }

    @DisplayName("클로바 서버 요청 시 FeignServerException이 발생하면 ExternalApiServerException이 throw 된다.")
    @Test
    void checkClovaServerException() {
        // given
        stubFor(post("/")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("clova server 500 error test body")
                )
        );

        // when & then
        assertThatThrownBy(() -> clovaService.sendWeeklyReportRequest(ClovaWeeklyReportRequestDto.from("테스트용")))
                .isExactlyInstanceOf(ExternalApiServerException.class);
    }
}