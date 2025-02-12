package site.radio.report.daily.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.common.aop.transaction.NamedLock;
import site.radio.error.DailyReportAlreadyExistsException;
import site.radio.error.DailyReportNotFoundException;
import site.radio.reply.dto.DailyLetters;
import site.radio.reply.service.LetterService;
import site.radio.report.daily.domain.LetterAnalysis;
import site.radio.report.daily.dto.DailyAnalysisResult;
import site.radio.report.daily.dto.DailyReportResponse;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.daily.repository.LetterAnalysisRepository;


@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository dailyReportRepository;
    private final LetterAnalysisRepository letterAnalysisRepository;
    private final LetterService letterService;
    private final LetterAnalysisService letterAnalysisService;

    /**
     * <ol> 이 메서드는 순차대로 아래 작업을 수행합니다.
     *     <li>전달받은 유저 아이디와 대상 날짜에 해당하는 일일 리포트가 이미 존재하는지 확인합니다.</li>
     *     <li>가장 최근 편지 3개를 찾아 분석합니다. 오늘이라면 현재 시점 기준, 오늘 이전이라면 해당 날짜 기준 가장 최근 편지 3개를 조회합니다.</li>
     *     <li>클로바에게 조회된 편지로 일일 리포트를 생성을 요청합니다.</li>
     *     <li>분석된 일일 리포트를 저장하고 응답합니다.</li>
     * </ol>
     *
     * @param userId     UUID 형식의 사용자 아이디
     * @param targetDate 리포트 생성 대상 날짜
     * @return 생성된 일일 리포트에 대한 응답 DTO
     */
    @CacheEvict(value = {"dailyReportStatus", "weeklyReportStatus"}, cacheManager = "caffeineCacheManager",
            key = "#userId.toString() + #targetDate.withDayOfMonth(targetDate.lengthOfMonth()).toString()")
    @NamedLock(lockName = "createdDailyReport", timeout = 0, keyFields = {"userId"})
    public DailyReportResponse createDailyReport(UUID userId, LocalDate targetDate) {
        if (dailyReportRepository.existsByUserAndTargetDate(userId, targetDate)) {
            throw new DailyReportAlreadyExistsException("Duplicate daily report exists.");
        }
        // 하루치 분석 가능 편지 조회
        DailyLetters dailyLetters = letterService.findAnalyzableLetters(userId, targetDate);

        // 편지 분석 생성 요청
        DailyAnalysisResult dailyAnalysis = letterAnalysisService.createDailyAnalysis(dailyLetters);

        // 트랜잭션 안에서 편지 분석, 데일리 리포트 저장 요청
        List<LetterAnalysis> letterAnalyses = letterAnalysisService.saveAnalysisAndDailyReport(dailyLetters,
                dailyAnalysis);

        return letterAnalyses.stream()
                .findAny()
                .map(letterAnalysis -> DailyReportResponse.of(letterAnalysis.getDailyReport(), letterAnalyses))
                .orElseThrow(() -> new DailyReportNotFoundException("데일리 리포트를 찾지 못했습니다. 날짜: " + targetDate));
    }

    @Cacheable(value = "dailyReport", cacheManager = "caffeineCacheManager",
            key = "#userId.toString() + #targetDate.toString()")
    @Transactional(readOnly = true)
    public DailyReportResponse getDailyReport(UUID userId, LocalDate targetDate) {
        List<LetterAnalysis> letterAnalyses = letterAnalysisRepository.findLetterAnalysesByTargetDateAt(userId,
                targetDate);

        return letterAnalyses.stream()
                .findAny()
                .map(letterAnalysis -> DailyReportResponse.of(letterAnalysis.getDailyReport(), letterAnalyses))
                .orElseThrow(() -> new DailyReportNotFoundException("데일리 리포트를 찾지 못했습니다. 날짜: " + targetDate));
    }

    public void preCreateDailyReport(UUID userId, LocalDate startDate, LocalDate endDate) {
        // 편지 서비스에게 `분석 가능한 편지들` 찾기 위임
        List<DailyLetters> analyzableLetters = letterService.findAnalyzableLettersInRange(userId, startDate, endDate);

        // 편지 분석 서비스에게 `편지 분석`, `데일리 리포트` 생성 요청
        List<DailyAnalysisResult> results = letterAnalysisService.createAsyncDailyAnalyses(analyzableLetters);

        // 편지 분석 서비스에게 트랜잭션 내에서 저장 요청
        letterAnalysisService.saveAllAnalysesAndDailyReports(analyzableLetters, results);
    }
}
