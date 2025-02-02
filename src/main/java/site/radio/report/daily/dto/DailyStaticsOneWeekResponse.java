package site.radio.report.daily.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import site.radio.report.daily.domain.DailyReport;

@Getter
@Builder
public class DailyStaticsOneWeekResponse {

    private List<DailyReport> dailyReports;
    private DailyReportStatics staticsDto;

    public static DailyStaticsOneWeekResponse of(List<DailyReport> dailyReports,
                                                 DailyReportStatics staticsDto) {
        return DailyStaticsOneWeekResponse.builder()
                .dailyReports(dailyReports)
                .staticsDto(staticsDto)
                .build();
    }
}
