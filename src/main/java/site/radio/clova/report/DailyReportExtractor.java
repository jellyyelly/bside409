package site.radio.clova.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.radio.clova.dto.CreateResponse;

public class DailyReportExtractor {

    public static ClovaDailyAnalysisResult extract(CreateResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClovaDailyAnalysisResult clovaDailyAnalysisResult =
                    objectMapper.readValue(response.getResultMessage(), ClovaDailyAnalysisResult.class);
            rearrangeCoreEmotions(clovaDailyAnalysisResult);
            return clovaDailyAnalysisResult;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("답변 형식이 잘못되었습니다. 답변받은 데이터: " + response.getResultMessage(), e);
        }
    }

    private static void rearrangeCoreEmotions(ClovaDailyAnalysisResult analysisResult) {
        if (analysisResult.getLetterAnalyses().size() > 1) {
            return;
        }
        String dailyCoreEmotion = analysisResult.getDailyCoreEmotion();
        ClovaDailyAnalysisResult.LetterAnalysis letterAnalysis = analysisResult.getLetterAnalyses().get(0);
        if (dailyCoreEmotion != null) {
            if (!letterAnalysis.getCoreEmotions().get(0).equals(dailyCoreEmotion)) {
                letterAnalysis.getCoreEmotions().remove(dailyCoreEmotion);
                letterAnalysis.getCoreEmotions().add(0, dailyCoreEmotion);
            }
        }
    }
}

