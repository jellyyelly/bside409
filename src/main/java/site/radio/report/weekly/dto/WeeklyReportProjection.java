package site.radio.report.weekly.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import site.radio.report.daily.domain.CoreEmotion;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyReportProjection {

    private int weekOfYear;
    private String cheerUp;
    private int publishedCount;
    private int unpublishedCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CoreEmotion> coreEmotions;

    @QueryProjection
    public WeeklyReportProjection(int weekOfYear, String cheerUp, int publishedCount, int unpublishedCount,
                                  LocalDate startDate, LocalDate endDate, List<CoreEmotion> coreEmotions) {
        this.weekOfYear = weekOfYear;
        this.cheerUp = cheerUp;
        this.publishedCount = publishedCount;
        this.unpublishedCount = unpublishedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.coreEmotions = coreEmotions;
    }
}