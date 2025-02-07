package site.radio.report.retrieve.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.dto.DailyReportIdProjection;

@Getter
@RequiredArgsConstructor
public class DailyReportStatusResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private final LocalDate date;

    private final CoreEmotion coreEmotion;

    private final boolean available;

    public static DailyReportStatusResponse create(LocalDate date, List<DailyReportIdProjection> letters) {
        boolean analyzed = letters.stream()
                .anyMatch(letter -> letter.getDailyReportId() != null);

        CoreEmotion coreEmotion = letters.stream()
                .filter(letter -> letter.getDailyReportId() != null)
                .findAny()
                .map(DailyReportIdProjection::getCoreEmotion)
                .orElse(null);

        return new DailyReportStatusResponse(date, coreEmotion, !analyzed);
    }

    public static DailyReportStatusResponse createFalseStatus(LocalDate date) {
        return new DailyReportStatusResponse(date, null, false);
    }
}
