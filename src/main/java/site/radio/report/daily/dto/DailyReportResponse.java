package site.radio.report.daily.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReportResponse {

    private final LocalDate date;
    private final List<LetterAnalysisResult> letterAnalyses;
    private final String dailyCoreEmotion;
    private final String description;

    @Getter
    @Builder(access = AccessLevel.PRIVATE)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LetterAnalysisResult {
        private final UUID letterId;
        private final List<String> coreEmotions;
        private final List<String> sensitiveEmotions;
        private final String topic;
        private final LocalDateTime createdAt;

        public static LetterAnalysisResult of(LetterAnalysis letterAnalysis) {
            return LetterAnalysisResult.builder()
                    .letterId(letterAnalysis.getLetter().getId())
                    .coreEmotions(
                            letterAnalysis.getCoreEmotions().stream().map(Enum::name).collect(Collectors.toList()))
                    .sensitiveEmotions(letterAnalysis.getSensitiveEmotions())
                    .topic(letterAnalysis.getTopic())
                    .createdAt(letterAnalysis.getCreatedAt())
                    .build();
        }
    }

    public static DailyReportResponse of(DailyReport dailyReport, List<LetterAnalysis> letterAnalyses) {
        return DailyReportResponse.builder()
                .date(dailyReport.getTargetDate())
                .letterAnalyses(letterAnalyses.stream().map(LetterAnalysisResult::of).collect(Collectors.toList()))
                .dailyCoreEmotion(dailyReport.getCoreEmotion().name())
                .description(dailyReport.getDescription())
                .build();
    }
}
