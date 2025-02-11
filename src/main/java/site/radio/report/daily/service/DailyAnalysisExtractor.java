package site.radio.report.daily.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.radio.clova.dto.CreateResponse;
import site.radio.report.daily.dto.DailyAnalysisResult;

public class DailyAnalysisExtractor {

    public static DailyAnalysisResult extract(CreateResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            DailyAnalysisResult dailyAnalysisResult =
                    objectMapper.readValue(response.getResultMessage(), DailyAnalysisResult.class);
            dailyAnalysisResult.rearrangeCoreEmotions();
            return dailyAnalysisResult;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("답변 형식이 잘못되었습니다. 답변받은 데이터: " + response.getResultMessage(), e);
        }
    }
}

