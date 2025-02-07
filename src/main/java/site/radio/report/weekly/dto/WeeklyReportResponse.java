package site.radio.report.weekly.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.util.CustomDateUtils;
import site.radio.report.weekly.domain.WeeklyReport;

@Schema(description = "유저의 주간 분석 요청이 성공하면 받는 응답 DTO")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReportResponse {

    @Schema(description = "1년 기반의 N주차")
    private int weekOfYear;

    @Schema(description = "N월 N주차")
    private String weekName;

    @Schema(description = "N월 N주차의 시작 날짜")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @Schema(description = "N월 N주차의 마지막 날짜")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    @Schema(description = "데일리 리포트들의 CoreEmotion 배열")
    private List<CoreEmotion> coreEmotions;

    @Schema(description = "주간 분석에 사용된 편지 개수")
    private int published;

    @Schema(description = "주간 분석에 사용된 일기 개수")
    private int unPublished;

    @Schema(description = "위로 한 마디")
    private String cheerUp;

    public static WeeklyReportResponse from(WeeklyReport weeklyReport, List<CoreEmotion> coreEmotions) {
        return WeeklyReportResponse.builder()
                .weekOfYear(weeklyReport.getWeekOfYear())
                .weekName(CustomDateUtils.getWeekOfMonth(weeklyReport.getStartDate(), false))
                .startDate(weeklyReport.getStartDate())
                .endDate(weeklyReport.getEndDate())
                .coreEmotions(coreEmotions)
                .published(weeklyReport.getPublishedCount())
                .unPublished(weeklyReport.getUnpublishedCount())
                .cheerUp(weeklyReport.getCheerUp())
                .build();
    }

    public static WeeklyReportResponse from(WeeklyReportProjection projection) {
        return WeeklyReportResponse.builder()
                .weekOfYear(projection.getWeekOfYear())
                .weekName(CustomDateUtils.getWeekOfMonth(projection.getStartDate(), false))
                .startDate(projection.getStartDate())
                .endDate(projection.getEndDate())
                .coreEmotions(projection.getCoreEmotions())
                .published(projection.getPublishedCount())
                .unPublished(projection.getUnpublishedCount())
                .cheerUp(projection.getCheerUp())
                .build();
    }
}
