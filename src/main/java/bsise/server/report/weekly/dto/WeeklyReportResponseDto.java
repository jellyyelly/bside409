package bsise.server.report.weekly.dto;

import bsise.server.report.weekly.service.WeeklyDataManager;
import bsise.server.report.weekly.domain.WeeklyReport;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Tag(name = "")
@Schema(description = "유저의 주간 분석 요청이 성공하면 받는 응답 DTO")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReportResponseDto {

    @Schema(description = "1년 기반의 N주차")
    private int weekOfYear;

    @Schema(description = "N월 N주차")
    private String week_name;

    @Schema(description = "N월 N주차의 시작 날짜")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate startDate;

    @Schema(description = "N월 N주차의 마지막 날짜")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDate endDate;

    @Schema(description = "주간 분석에 사용된 편지 개수")
    private int published;

    @Schema(description = "주간 분석에 사용된 일기 개수")
    private int unPublished;

    @Schema(description = "위로 한 마디")
    private String cheerUp;

    public static WeeklyReportResponseDto from(WeeklyReport weeklyReport) {
        LocalDate target = weeklyReport.getStartDate();

        return WeeklyReportResponseDto.builder()
                .weekOfYear(weeklyReport.getWeekOfYear())
                .week_name(getMonthValue(target) + "월 " + getWeekOfMonth(target) + "주차")
                .startDate(weeklyReport.getStartDate())
                .endDate(weeklyReport.getEndDate())
                .published(weeklyReport.getPublishedCount())
                .unPublished(weeklyReport.getUnpublishedCount())
                .cheerUp(weeklyReport.getCheerUp())
                .build();
    }

    private static int getMonthValue(LocalDate target) {
        return new WeeklyDataManager(target).getMonthValue();
    }

    private static int getWeekOfMonth(LocalDate target) {
        return new WeeklyDataManager(target).getWeekOfMonth();
    }
}
