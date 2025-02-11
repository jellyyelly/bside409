package site.radio;

import static site.radio.report.daily.domain.CoreEmotion.기쁨;
import static site.radio.report.daily.domain.CoreEmotion.분노;
import static site.radio.report.daily.domain.CoreEmotion.중립;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import site.radio.report.daily.dto.DailyAnalysisResult;

class JsonStringToDailyAnalysisResultTest {

    @Test
    void readValue_validResponse_success() throws JsonProcessingException {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();

        String validResponseJson = """
                {
                    "letterAnalyses": [
                        {
                            "coreEmotions": ["슬픔", "분노"],
                            "sensitiveEmotions": [],
                            "topic": "과도한 학업에의 부담"
                        },
                        {
                            "coreEmotions": ["중립"],
                            "sensitiveEmotions": [],
                            "topic": "식사 기록"
                        }
                    ],
                    "dailyCoreEmotion": "슬픔",
                    "description": "공부와 코딩에 몰두하느라 바쁜 하루를 보내면서도 약간의 피로감을 느끼고 계시는군요."
                }
                """;

        // When
        DailyAnalysisResult dailyAnalysisResult = objectMapper.readValue(validResponseJson,
                DailyAnalysisResult.class);

        // Then
        Assertions.assertThat(dailyAnalysisResult).isNotNull();
        Assertions.assertThat(dailyAnalysisResult.getDailyCoreEmotion()).isEqualTo("슬픔");
        Assertions.assertThat(dailyAnalysisResult.getEmotionAnalyses()).hasSize(2);

        // 첫 번째 letterAnalyses 검증
        Assertions.assertThat(dailyAnalysisResult.getEmotionAnalyses().get(0).getCoreEmotions())
                .containsExactlyInAnyOrder(기쁨, 분노);
        Assertions.assertThat(dailyAnalysisResult.getEmotionAnalyses().get(0).getTopic())
                .isEqualTo("과도한 학업에의 부담");

        // 두 번째 letterAnalyses 검증
        Assertions.assertThat(dailyAnalysisResult.getEmotionAnalyses().get(1).getCoreEmotions())
                .containsExactly(중립);
        Assertions.assertThat(dailyAnalysisResult.getEmotionAnalyses().get(1).getTopic())
                .isEqualTo("식사 기록");
    }
}
