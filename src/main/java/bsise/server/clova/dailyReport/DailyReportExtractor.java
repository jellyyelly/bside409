package bsise.server.clova.dailyReport;

import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.report.daily.domain.CoreEmotion;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.stream.Collectors;

public class DailyReportExtractor {

    public static ClovaDailyAnalysisResult extract(ClovaResponseDto response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClovaDailyAnalysisResult clovaDailyAnalysisResult = objectMapper.readValue(response.getResultMessage(), ClovaDailyAnalysisResult.class);

            // 감정 목록에 없는 감정 필터링
            Set<String> invalidEmotions = clovaDailyAnalysisResult.getLetterAnalyses().stream()
                    .flatMap(analysis -> analysis.getCoreEmotions().stream())
                    .filter(CoreEmotion::isAbsent)
                    .collect(Collectors.toSet());

            // 일일 대표 감정 검증
            boolean dailyCoreEmotionInvalid = CoreEmotion.isAbsent(clovaDailyAnalysisResult.getDailyCoreEmotion());

            if (!invalidEmotions.isEmpty() || dailyCoreEmotionInvalid) {
                throw new IllegalArgumentException("대표 감정 목록에 존재하지 않는 감정이 분석되었습니다: " + invalidEmotions);
            }

            return clovaDailyAnalysisResult;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("답변 형식이 잘못되었습니다. 답변받은 데이터: " + response.getResultMessage(), e);
        }
    }
}

