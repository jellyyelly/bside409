package site.radio.report.daily.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClovaDailyAnalysisResult {

    @JsonProperty(value = "letterAnalyses")
    private List<LetterAnalysis> letterAnalyses;

    @JsonProperty(value = "dailyCoreEmotion")
    private String dailyCoreEmotion;

    @JsonProperty(value = "description")
    private String description;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class LetterAnalysis {
        @JsonProperty(value = "seq")
        private int seq;

        @JsonProperty(value = "coreEmotions")
        private List<String> coreEmotions;

        @JsonProperty(value = "sensitiveEmotions")
        private List<String> sensitiveEmotions;

        @JsonProperty(value = "topic")
        private String topic;
    }
}