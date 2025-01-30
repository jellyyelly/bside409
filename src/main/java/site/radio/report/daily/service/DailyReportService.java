package site.radio.report.daily.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.report.ClovaDailyAnalysisResult;
import site.radio.clova.report.DailyReportExtractor;
import site.radio.clova.service.ClovaService;
import site.radio.common.aop.transaction.NamedLock;
import site.radio.error.DailyReportAlreadyExistsException;
import site.radio.error.DailyReportNotFoundException;
import site.radio.error.LetterNotFoundException;
import site.radio.letter.Letter;
import site.radio.letter.LetterRepository;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;
import site.radio.report.daily.dto.DailyReportResponseDto;
import site.radio.report.daily.dto.DailyReportStaticsDto;
import site.radio.report.daily.dto.DailyStaticsOneWeekResponseDto;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.daily.repository.LetterAnalysisRepository;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DailyReportService {

    private static final String LETTER_SEPARATOR = "sharpie-sep";

    private final DailyReportPromptTemplate promptTemplate;
    private final DailyReportRepository dailyReportRepository;
    private final LetterRepository letterRepository;
    private final LetterAnalysisRepository letterAnalysisRepository;
    private final ClovaService clovaService;

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
    public DailyReportResponseDto createDailyReport(UUID userId, LocalDate targetDate) {
        if (dailyReportRepository.existsByUserAndTargetDate(userId, targetDate)) {
            throw new DailyReportAlreadyExistsException("Duplicate daily report exists.");
        }
        List<Letter> letters = findRecentLetters(userId, targetDate);

        // 클로바에 분석 요청
        CreateResponse clovaResponse = requestClovaAnalysis(letters);

        // 클로바 응답 파싱
        ClovaDailyAnalysisResult clovaDailyAnalysisResult = DailyReportExtractor.extract(clovaResponse);

        // 데일리 리포트 저장
        DailyReport dailyReport = buildDailyReport(targetDate, clovaDailyAnalysisResult);
        dailyReportRepository.save(dailyReport);

        // 감정 분석 저장
        List<LetterAnalysis> letterAnalyses = buildLetterAnalyses(letters, clovaDailyAnalysisResult);
        letterAnalyses.forEach(analysis -> analysis.getLetter().setDailyReport(dailyReport));
        letterAnalysisRepository.saveAll(letterAnalyses);

        return DailyReportResponseDto.of(dailyReport, letterAnalyses);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public DailyReportResponseDto createDailyReportWithFacade(UUID userId, LocalDate targetDate) {
        if (dailyReportRepository.existsByUserAndTargetDate(userId, targetDate)) {
            throw new DailyReportAlreadyExistsException("Duplicate daily report exists.");
        }
        List<Letter> letters = findRecentLetters(userId, targetDate);

        // 클로바에 분석 요청
        CreateResponse clovaResponse = requestClovaAnalysis(letters);

        // 클로바 응답 파싱
        ClovaDailyAnalysisResult clovaDailyAnalysisResult = DailyReportExtractor.extract(clovaResponse);

        // 데일리 리포트 저장
        DailyReport dailyReport = buildDailyReport(targetDate, clovaDailyAnalysisResult);
        dailyReportRepository.save(dailyReport);

        // 감정 분석 저장
        List<LetterAnalysis> letterAnalyses = buildLetterAnalyses(letters, clovaDailyAnalysisResult);
        letterAnalyses.forEach(analysis -> analysis.getLetter().setDailyReport(dailyReport));
        letterAnalysisRepository.saveAll(letterAnalyses);

        return DailyReportResponseDto.of(dailyReport, letterAnalyses);
    }

    public DailyReportResponseDto getDailyReport(UUID userId, LocalDate targetDate) {
        DailyReport dailyReport = dailyReportRepository.findByUserAndTargetDate(userId, targetDate)
                .orElseThrow(
                        () -> new DailyReportNotFoundException("Daily Report not found. targetDate: " + targetDate));

        List<LetterAnalysis> letterAnalyses = letterAnalysisRepository.findByDailyReportId(dailyReport.getId());

        return DailyReportResponseDto.of(dailyReport, letterAnalyses);
    }

    public void createDailyReportsBy(UUID userId, LocalDate startDate, LocalDate endDate) {
        // 주간분석을 요청한 기간 동안 사용자가 작성한 편지들 찾기
        List<Letter> userLettersByLatest = letterRepository.findByCreatedAtDesc(userId,
                startDate.atStartOfDay(),
                LocalDateTime.of(endDate, LocalTime.MAX));

        // 날짜별로 편지들을 3개씩 묶기
        Map<LocalDate, List<Letter>> latestLettersByDate = userLettersByLatest.stream()
                .collect(Collectors.groupingBy(
                        letter -> letter.getCreatedAt().toLocalDate()));

        // 이미 일일 분석이 생성된 날짜는 제거
        latestLettersByDate.values().removeIf(
                letters -> letters.stream().anyMatch(letter -> letter.getDailyReport() != null)
        );

        // 일일 분석을 생성하려는 편지들을 날짜당 3개로 제한
        Map<LocalDate, List<Letter>> latestThreeLettersByDate = latestLettersByDate.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(3)
                                .collect(Collectors.toList())
                ));

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

    public DailyStaticsOneWeekResponseDto findDailyStaticsInOneWeek(UUID userId, List<LocalDate> oneWeekDates) {
        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(userId, oneWeekDates);
        DailyReportStaticsDto dto = dailyReportRepository.findStaticsBy(userId, oneWeekDates);

        return DailyStaticsOneWeekResponseDto.of(dailyReports, dto);
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

    private DailyReport buildDailyReport(LocalDate targetDate, ClovaDailyAnalysisResult clovaDailyAnalysisResult) {
        return DailyReport.builder()
                .targetDate(targetDate)
                .coreEmotion(CoreEmotion.findOrNeutral(clovaDailyAnalysisResult.getDailyCoreEmotion()))
                .description(clovaDailyAnalysisResult.getDescription())
                .build();
    }

    private List<LetterAnalysis> buildLetterAnalyses(List<Letter> letters,
                                                     ClovaDailyAnalysisResult clovaDailyAnalysisResult) {
        return clovaDailyAnalysisResult.getLetterAnalyses().stream()
                .map(analysis -> {
                    int index = clovaDailyAnalysisResult.getLetterAnalyses().indexOf(analysis);
                    Letter letter = letters.get(index); // 순서대로 letter 매핑

                    return LetterAnalysis.builder()
                            .letter(letter)
                            .topic(analysis.getTopic())
                            .coreEmotions(analysis.getCoreEmotions().stream()
                                    .map(CoreEmotion::findOrNeutral)
                                    .collect(Collectors.toList()))
                            .sensitiveEmotions(analysis.getSensitiveEmotions())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
