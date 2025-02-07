package site.radio.report.daily.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.common.aop.transaction.NamedLock;
import site.radio.error.DailyReportAlreadyExistsException;
import site.radio.error.DailyReportNotFoundException;
import site.radio.error.LetterNotFoundException;
import site.radio.reply.domain.Letter;
import site.radio.reply.dto.DailyLetters;
import site.radio.reply.repository.LetterRepository;
import site.radio.reply.service.LetterService;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;
import site.radio.report.daily.dto.DailyAnalysisResult;
import site.radio.report.daily.dto.DailyReportResponse;
import site.radio.report.daily.dto.DailyReportStatics;
import site.radio.report.daily.dto.DailyStaticsOneWeekResponse;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.daily.repository.LetterAnalysisRepository;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportPromptTemplate promptTemplate;
    private final DailyReportRepository dailyReportRepository;
    private final LetterRepository letterRepository;
    private final LetterAnalysisRepository letterAnalysisRepository;
    private final ClovaService clovaService;
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
    @NamedLock(lockName = "createdDailyReport", timeout = 0, keyFields = {"userId"})
    public DailyReportResponse createDailyReport(UUID userId, LocalDate targetDate) {
        if (dailyReportRepository.existsByUserAndTargetDate(userId, targetDate)) {  //TODO: LetterAnalysis repo로 메서드 이동
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

    public DailyReportResponse getDailyReport(UUID userId, LocalDate targetDate) {
        List<LetterAnalysis> letterAnalyses = letterAnalysisRepository.findLetterAnalysesByTargetDateAt(userId,
                targetDate);

        return letterAnalyses.stream()
                .findAny()
                .map(letterAnalysis -> DailyReportResponse.of(letterAnalysis.getDailyReport(), letterAnalyses))
                .orElseThrow(() -> new DailyReportNotFoundException("데일리 리포트를 찾지 못했습니다. 날짜: " + targetDate));
    }

    public void createDailyReportsBy(UUID userId, LocalDate startDate, LocalDate endDate) {
        // 편지 서비스에게 `분석 가능한 편지들` 찾기 위임
        List<DailyLetters> analyzableLetters = letterService.findAnalyzableLettersInRange(userId, startDate, endDate);

        for (DailyLetters dailyLetters : analyzableLetters) {
            // 외부 API 호출
            CreateResponse createResponse = clovaService.sendWithPromptTemplate(promptTemplate,
                    dailyLetters.getMessages());

            // 응답 결과로부터 하루치 분석 추출
            DailyAnalysisResult analysisResult = DailyAnalysisExtractor.extract(createResponse);

            // 하루치 분석으로부터 편지 분석, 데일리 리포트 엔티티 저장
            letterAnalysisService.saveAnalysisAndDailyReport(dailyLetters, analysisResult);
        }
    }

        // 편지 3개에 대한 분석을 Clova에게 요청해서 받은 결과물들
        Map<ClovaDailyAnalysisResult, List<Letter>> lettersByAnalysisResult = latestThreeLettersByDate.values().stream()
                .collect(Collectors.toMap(
                        letters -> DailyReportExtractor.extract(requestClovaAnalysis(letters)),
                        letters -> letters
                ));

        // 분석결과와 편지들을 가지고 데일리 리포트 생성
        Map<DailyReport, List<Letter>> lettersByDailyReport = lettersByAnalysisResult.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> buildDailyReport(entry.getValue().get(0).getCreatedAt().toLocalDate(), entry.getKey()),
                        Entry::getValue
                ));
        dailyReportRepository.saveAll(lettersByDailyReport.keySet());

        // 편지들에 알맞는 데일리 리포트를 setter 주입
        lettersByDailyReport.forEach((key, value) ->
                value.forEach(
                        letter -> letter.setDailyReport(key)
                ));

        // 편지와 분석결과를 가지고 편지분석엔티티들 생성 및 저장
        List<LetterAnalysis> letterAnalyses = lettersByAnalysisResult.entrySet().stream()
                .flatMap(entry -> buildLetterAnalyses(entry.getValue(), entry.getKey()).stream())
                .toList();
        letterAnalysisRepository.saveAll(letterAnalyses);
    }

    public DailyStaticsOneWeekResponse findDailyStaticsInOneWeek(UUID userId, List<LocalDate> oneWeekDates) {
        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(userId, oneWeekDates);
        DailyReportStatics dto = dailyReportRepository.findStaticsBy(userId, oneWeekDates);

        return DailyStaticsOneWeekResponse.of(dailyReports, dto);
    }

    /**
     * <ol> userId와 targetDate에 해당하는 편지를 최대 3건 조회합니다.
     *     <li>만약 오늘이라면, 현재 시점 기준 가장 최근 3건</li>
     *     <li>만약 오늘 이전이라면, 해당 날짜의 가장 마지막 3건</li>
     * </ol>
     *
     * @param userId     일일분석 요청하는 사용자 아이디
     * @param targetDate 일일분석 요청할 날짜
     * @return 편지 3건 리스트
     */
    private List<Letter> findRecentLetters(UUID userId, LocalDate targetDate) {
        LocalDateTime endTime = targetDate.isEqual(LocalDate.now())
                ? LocalDateTime.now()
                : targetDate.atTime(LocalTime.MAX);

        List<Letter> letters = letterRepository.find3RecentLetters(userId, targetDate.atStartOfDay(), endTime);

        if (letters.isEmpty()) {
            throw new LetterNotFoundException("Letters for daily analysis not found.");
        }
        return letters;
    }

    private CreateResponse requestClovaAnalysis(List<Letter> letters) {
        // 편지 내용 구분자 동적 생성
        String msgSeparator = Long.toHexString(Double.doubleToLongBits(Math.random()));

        String formattedMessages = letters.stream()
                .map(letter -> String.format("<%s:%s>\n%s\n</%s:%s>",
                        LETTER_SEPARATOR, msgSeparator,
                        reformatMsg(letter.getMessage()),
                        LETTER_SEPARATOR, msgSeparator))
                .collect(Collectors.joining("\n"));

        return clovaService.sendWithPromptTemplate(promptTemplate, formattedMessages);
    }

    private String reformatMsg(String input) {
        return input
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("&", "&amp;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&apos;");
    }

    private DailyReport buildDailyReport(LocalDate targetDate, DailyAnalysisResult dailyAnalysisResult) {
        return DailyReport.builder()
                .targetDate(targetDate)
                .coreEmotion(dailyAnalysisResult.getDailyCoreEmotion())
                .description(dailyAnalysisResult.getDescription())
                .build();
    }

    private List<LetterAnalysis> buildLetterAnalyses(List<Letter> letters,
                                                     DailyReport dailyReport,
                                                     DailyAnalysisResult dailyAnalysisResult) {
        return dailyAnalysisResult.getEmotionAnalyses().stream()
                .map(analysis -> {
                    int index = dailyAnalysisResult.getEmotionAnalyses().indexOf(analysis);
                    Letter letter = letters.get(index); // 순서대로 letter 매핑

                    return LetterAnalysis.builder()
                            .letter(letter)
                            .dailyReport(dailyReport)
                            .topic(analysis.getTopic())
                            .coreEmotions(analysis.getCoreEmotions())
                            .sensitiveEmotions(analysis.getSensitiveEmotions())
                            .build();
                })
                .collect(Collectors.toList());
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
