package site.radio.report.retrieve.dto;

import static site.radio.report.util.CustomDateUtils.getWeekOfMonth;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.report.weekly.dto.WeeklyReportIdProjection;

@Getter
@RequiredArgsConstructor
public class WeeklyReportStatusResponse {

    private final int weekOfYear;

    private final String weekName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private final LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private final LocalDate endDate;

    private final int totalCount; // 해당 주차에 속하는 편지 개수

    private final boolean available;

    private final boolean analyzed;

    public static WeeklyReportStatusResponse create(int weekOfYear, List<LocalDate> dates,
                                                    List<WeeklyReportIdProjection> reports) {
        boolean analyzed = reports.stream()
                .anyMatch(report -> report.getWeeklyReportId() != null);

        LocalDate startDate = dates.get(0).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = dates.get(0).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return new WeeklyReportStatusResponse(
                weekOfYear, getWeekOfMonth(startDate, true), startDate, endDate, reports.size(), !analyzed, analyzed
        );
    }

    public static WeeklyReportStatusResponse createFalseStatus(int weekOfYear, List<LocalDate> dates) {
        LocalDate startDate = dates.get(0).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = dates.get(0).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return new WeeklyReportStatusResponse(
                weekOfYear, getWeekOfMonth(startDate, true), startDate, endDate, 0, false, false
        );
    }
}
