package bsise.server.report.monthly.dto;

import bsise.server.report.daily.dto.DailyReportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class MonthlyReportResultResponseDto {

    private final List<DailyReportStatusDto> reports;

    @Getter
    @Builder
    @RequiredArgsConstructor
    public static class DailyReportStatusDto {

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        private final LocalDate date;

        private final int totalCount;

        private final String dailyCoreEmotion;

        private final boolean available;

        private final boolean analyzed;

        private final String weeklyReportId;

        public static DailyReportStatusDto from(DailyReportStatus dailyReportStatus, boolean available, boolean analyzed) {
            return DailyReportStatusDto.builder()
                    .date(dailyReportStatus.getReportDate())
                    .totalCount(dailyReportStatus.getLetterCount())
                    .dailyCoreEmotion(dailyReportStatus.getDailyCoreEmotion())
                    .weeklyReportId(dailyReportStatus.getWeeklyReportId())
                    .available(available)
                    .analyzed(analyzed)
                    .build();
        }
    }
}
