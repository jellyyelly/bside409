package bsise.server.clova.service;

import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.clova.client.ClovaFeignClient;
import bsise.server.clova.dailyReport.DummyDailyReportClovaResponseDto;
import bsise.server.report.weekly.dto.ClovaWeeklyReportRequestDto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Clova API 를 사용하지 않고, 더미 데이터를 응답하는 더미 서비스입니다.
 * 테스트 목적으로 생성되었으며, 운영 환경에서 사용할 수 없습니다.
 */
@Profile("test")
@Service
public class DummyReportClovaService extends ClovaService {

    public DummyReportClovaService(ClovaFeignClient client) {
        super(client);
    }

    /**
     * 일반 편지를 생성할 때 사용하는 메서드를 대체합니다. 테스트 시 사용하지 않기 때문에 {@code null}을 반환하는 것을 유의해야 합니다.
     * @param message 이 파라미터를 받아도 사용되지 않습니다.
     * @return {@code null}을 반환합니다.
     */
    @Override
    public ClovaResponseDto send(String message) {
        return null;
    }

    /**
     * message 를 구분자({@code ,})를 이용해 분리한 뒤, 편지 개수에 맞는 응답을 생성합니다. (최대 3개)
     * @param message 편지의 내용을 구분자({@code ,})를 통해 하나로 만든 문자열
     * @return 더미 Clova 응답
     */
    @Override
    public ClovaResponseDto sendDailyReportRequest(String message) {
        int lettersCount = message.split(",").length;
        return DummyDailyReportClovaResponseDto.createDummy(lettersCount);
    }

    // TODO: weeklyReport 구현 선행
    @Override
    public ClovaResponseDto sendWeeklyReportRequest(ClovaWeeklyReportRequestDto dto) {
        return null;
    }
}
