package site.radio.clova.dailyReport;

import java.util.List;
import java.util.stream.Collectors;
import site.radio.clova.dto.ClovaResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.radio.letter.Letter;

public class DailyReportMessageParser {

    private static final String LETTER_SEPARATOR = "sharpie-sep";

    public static ClovaDailyAnalysisResult extract(ClovaResponseDto response) {
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
        if(analysisResult.getLetterAnalyses().size() > 1) {
            return;
        }
        String dailyCoreEmotion = analysisResult.getDailyCoreEmotion();
        ClovaDailyAnalysisResult.LetterAnalysis letterAnalysis = analysisResult.getLetterAnalyses().get(0);
        if(dailyCoreEmotion != null) {
            if(!letterAnalysis.getCoreEmotions().get(0).equals(dailyCoreEmotion)) {
                letterAnalysis.getCoreEmotions().remove(dailyCoreEmotion);
                letterAnalysis.getCoreEmotions().add(0, dailyCoreEmotion);
            }
        }
    }

    public static String requestClovaAnalysis(List<Letter> letters) {
        // 편지 내용 구분자 동적 생성
        String msgSeparator = Long.toHexString(Double.doubleToLongBits(Math.random()));

        return letters.stream()
                .map(letter -> String.format("<%s:%s>\n%s\n</%s:%s>",
                        LETTER_SEPARATOR, msgSeparator,
                        reformatMsg(letter.getMessage()),
                        LETTER_SEPARATOR, msgSeparator))
                .collect(Collectors.joining("\n"));
    }

    private static String reformatMsg(String input) {
        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;");
    }
}
