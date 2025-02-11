package site.radio.report.daily.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.report.daily.domain.CoreEmotion;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyAnalysisResult {

    @JsonProperty(value = "letterAnalyses")
    private List<EmotionAnalysis> emotionAnalyses;

    @JsonProperty(value = "dailyCoreEmotion")
    private String dailyCoreEmotion;

    @JsonProperty(value = "description")
    private String description;

    public CoreEmotion getDailyCoreEmotion() {
        return CoreEmotion.findOrNeutral(dailyCoreEmotion);
    }

    public void rearrangeCoreEmotions() {
        if (emotionAnalyses == null || emotionAnalyses.size() != 1) {
            return;
        }
        CoreEmotion dailyCoreEmotion = getDailyCoreEmotion();
        EmotionAnalysis emotionAnalysis = emotionAnalyses.get(0);
        if (dailyCoreEmotion != null) {
            emotionAnalysis.rearrangeCoreEmotions(dailyCoreEmotion);
        }
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class EmotionAnalysis {

        @JsonProperty(value = "seq")
        private int sequence;

        @JsonProperty(value = "coreEmotions")
        private List<String> coreEmotions;

        @JsonProperty(value = "sensitiveEmotions")
        private List<String> sensitiveEmotions;

        @JsonProperty(value = "topic")
        private String topic;

        public List<CoreEmotion> getCoreEmotions() {
            return coreEmotions.stream()
                    .map(CoreEmotion::findOrNeutral)
                    .toList();
        }

        private void rearrangeCoreEmotions(CoreEmotion dailyCoreEmotion) {
            List<CoreEmotion> mutableCoreEmotions = new ArrayList<>(getCoreEmotions());
            if (!mutableCoreEmotions.isEmpty() && !mutableCoreEmotions.get(0).equals(dailyCoreEmotion)) {
                mutableCoreEmotions.remove(dailyCoreEmotion);
                mutableCoreEmotions.add(0, dailyCoreEmotion);
                setCoreEmotions(mutableCoreEmotions);
            }
        }

        private void setCoreEmotions(List<CoreEmotion> newCoreEmotions) {
            this.coreEmotions = newCoreEmotions.stream()
                    .map(CoreEmotion::name)
                    .toList();
        }
    }
}