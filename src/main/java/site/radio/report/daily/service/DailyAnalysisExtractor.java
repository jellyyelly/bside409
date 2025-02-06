package site.radio.report.daily.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.radio.clova.dto.CreateResponse;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.dto.DailyAnalysisResult;
import site.radio.report.daily.dto.DailyAnalysisResult.EmotionAnalysis;

public class DailyAnalysisExtractor {

    public static DailyAnalysisResult extract(CreateResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            DailyAnalysisResult dailyAnalysisResult =
                    objectMapper.readValue(response.getResultMessage(), DailyAnalysisResult.class);
            rearrangeCoreEmotions(dailyAnalysisResult);
            return dailyAnalysisResult;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("답변 형식이 잘못되었습니다. 답변받은 데이터: " + response.getResultMessage(), e);
        }
    }

    private static void rearrangeCoreEmotions(DailyAnalysisResult analysisResult) {
        if (analysisResult.getEmotionAnalyses().size() > 1) {
            return;
        }
        CoreEmotion dailyCoreEmotion = analysisResult.getDailyCoreEmotion();
        EmotionAnalysis emotionAnalysis = analysisResult.getEmotionAnalyses().get(0);
        if (dailyCoreEmotion != null) {
            if (!emotionAnalysis.getCoreEmotions().get(0).equals(dailyCoreEmotion)) {
                emotionAnalysis.getCoreEmotions().remove(dailyCoreEmotion);
                emotionAnalysis.getCoreEmotions().add(0, dailyCoreEmotion);
            }
        }
    }
}

