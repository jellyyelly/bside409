package site.radio.report.daily.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.reply.dto.DailyLetters;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;
import site.radio.report.daily.dto.DailyAnalysisResult;
import site.radio.report.daily.dto.DailyAnalysisResult.EmotionAnalysis;
import site.radio.report.daily.repository.LetterAnalysisRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class LetterAnalysisService {

    private final ClovaService clovaService;
    private final DailyReportPromptTemplate promptTemplate;
    private final LetterAnalysisRepository letterAnalysisRepository;

    /**
     * '하루치 편지들'에 대한 감정 분석 생성 요청을 클로바 서비스에게 위입합니다.
     *
     * @param dailyLetters 분석에 사용될 하루치 편지들
     * @return 편지들에 대한 분석 결과
     */
    public DailyAnalysisResult createDailyAnalysis(DailyLetters dailyLetters) {
        CreateResponse createResponse = clovaService.sendWithPromptTemplate(promptTemplate, dailyLetters.getMessages());

        return DailyAnalysisExtractor.extract(createResponse);
    }

    /**
     * 분석에 사용된 편지들과 그에 대응하는 분석 결과를 매핑하고, 데이터 액세스 계층에 저장을 위임합니다.
     *
     * @param dailyLetters        분석에 사용될 하루치 편지들
     * @param dailyAnalysisResult 편지들에 대한 분석 결과
     */
    @Transactional
    public List<LetterAnalysis> saveAnalysisAndDailyReport(DailyLetters dailyLetters,
                                                           DailyAnalysisResult dailyAnalysisResult) {
        // 데일리 리포트 엔티티 생성
        DailyReport dailyReport = DailyReport.builder()
                .coreEmotion(dailyAnalysisResult.getDailyCoreEmotion())
                .description(dailyAnalysisResult.getDescription())
                .targetDate(dailyLetters.getCreatedDate())
                .build();

        // 감정 분석 추출
        List<EmotionAnalysis> emotionAnalyses = dailyAnalysisResult.getEmotionAnalyses();

        // 편지 분석 엔티티 (연관 관계 주인: cascade.PERSIST)
        List<LetterAnalysis> letterAnalyses = emotionAnalyses.stream()
                .map(emotionAnalysis -> LetterAnalysis.builder()
                        .letter(dailyLetters.getLetter(emotionAnalysis.getSequence()))
                        .dailyReport(dailyReport)
                        .topic(emotionAnalysis.getTopic())
                        .coreEmotions(emotionAnalysis.getCoreEmotions())
                        .sensitiveEmotions(emotionAnalysis.getSensitiveEmotions())
                        .build())
                .toList();

        return letterAnalysisRepository.saveAll(letterAnalyses);
    }

    public List<DailyAnalysisResult> createAsyncDailyAnalyses(List<DailyLetters> dailyLetters) {
        List<CompletableFuture<DailyAnalysisResult>> futures = dailyLetters.stream()
                .map(each -> clovaService.sendAsyncWithPromptTemplate(promptTemplate, each.getMessages())
                        .thenApply(DailyAnalysisExtractor::extract))
                .toList();

        CompletableFuture<List<DailyAnalysisResult>> combinedFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());

        return combinedFuture.join();
    }

    @Transactional
    public void saveAllAnalysesAndDailyReports(List<DailyLetters> dailyLetters, List<DailyAnalysisResult> results) {
        for (int i = 0; i < results.size(); i++) {
            saveAnalysisAndDailyReport(dailyLetters.get(i), results.get(i));
        }
    }
}
