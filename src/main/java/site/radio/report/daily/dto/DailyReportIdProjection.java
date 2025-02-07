package site.radio.report.daily.dto;

import java.time.LocalDateTime;
import site.radio.report.daily.domain.CoreEmotion;

public interface DailyReportIdProjection {

    String getDailyReportId();

    CoreEmotion getCoreEmotion();

    LocalDateTime getLetterCreatedAt();
}
