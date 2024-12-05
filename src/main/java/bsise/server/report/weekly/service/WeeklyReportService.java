package bsise.server.report.weekly.service;

import bsise.server.clova.dailyReport.ClovaDailyAnalysisResult;
import bsise.server.clova.dailyReport.DailyReportExtractor;
import bsise.server.clova.dto.ClovaResponseDto;
import bsise.server.clova.service.ClovaService;
import bsise.server.clova.weekly.ClovaWeeklyReportRequestDto;
import bsise.server.error.DuplicationWeeklyReportException;
import bsise.server.error.WeeklyReportNotFoundException;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.report.daily.domain.CoreEmotion;
import bsise.server.report.daily.domain.DailyReport;
import bsise.server.report.daily.domain.LetterAnalysis;
import bsise.server.report.daily.repository.DailyReportRepository;
import bsise.server.report.daily.repository.LetterAnalysisRepository;
import bsise.server.report.weekly.domain.WeeklyReport;
import bsise.server.report.weekly.dto.WeeklyPublishedStaticsDto;
import bsise.server.report.weekly.dto.WeeklyReportRequestDto;
import bsise.server.report.weekly.dto.WeeklyReportResponseDto;
import bsise.server.report.weekly.repository.WeeklyReportRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WeeklyReportService {

    private final DailyReportRepository dailyReportRepository;
    private final LetterRepository letterRepository;
    private final LetterAnalysisRepository letterAnalysisRepository;
    private final ClovaService clovaService;
    private final WeeklyReportRepository weeklyReportRepository;

    /**
     * 분석 날짜, 작성빈도, 요일 ⛧ 감정 변화 추이, 위로 메세지, + 데일리의 각 편지 대표 감정
     * 일요일 자정(00:00)을 넘으면 주간 분석 요청 버튼 활성화 및 주간 분석 요청 가능
     */
    public WeeklyReportResponseDto createWeeklyReport(WeeklyReportRequestDto weeklyReportRequestDto) {
        // 주간 분석 생성할 수 있는지 검증
        weeklyReportRepository.findDailyReportBy(
                UUID.fromString(weeklyReportRequestDto.getUserId()),
                weeklyReportRequestDto.getStartDate(),
                weeklyReportRequestDto.getStartDate().plusDays(6)
        ).ifPresent(report -> {
            throw new DuplicationWeeklyReportException("주간 분석이 이미 존재합니다");
        });

        // 주간분석을 요청한 기간 동안 사용자가 작성한 편지들 찾기
        LocalDateTime start = weeklyReportRequestDto.getStartDate().atStartOfDay();
        LocalDateTime end = start.plusDays(7);

        List<Letter> userLettersByLatest = letterRepository.findByCreatedAtDesc(
                UUID.fromString(weeklyReportRequestDto.getUserId()),
                start,
                end
        );

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

        // startDate 로 부터 1주일 날짜 구하기
        List<LocalDate> oneWeekDates = createOneWeek(weeklyReportRequestDto.getStartDate());

        // 1주일에 해당하는 데일리 리포트 찾기
        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(oneWeekDates);

        //dailyReport 에서 설명 합치기
        String descriptions = dailyReports.stream()
                .map(DailyReport::getDescription)
                .collect(Collectors.joining());

        ClovaResponseDto clovaResponseDto = clovaService.sendWeeklyReportRequest(
                ClovaWeeklyReportRequestDto.from(descriptions));
        String resultMessage = clovaResponseDto.getResultMessage();

        WeeklyDataManager manager = new WeeklyDataManager(weeklyReportRequestDto.getStartDate());
        WeeklyPublishedStaticsDto staticsDto = dailyReportRepository.findPublishedStatics(oneWeekDates);

        WeeklyReport weeklyReport = WeeklyReport.builder()
                .weekOfYear(manager.getWeekOfWeekBasedYear())
                .startDate(manager.getMondayOfWeek())
                .endDate(manager.getSundayOfWeek())
                .cheerUp(resultMessage)
                .publishedCount(staticsDto.getPublishedCount())
                .unpublishedCount(staticsDto.getUnPublishedCount())
                .build();
        weeklyReportRepository.save(weeklyReport);
        dailyReports.forEach(dailyReport -> dailyReport.setWeeklyReport(weeklyReport));

        return WeeklyReportResponseDto.from(weeklyReport);
    }

    public WeeklyReportResponseDto getWeeklyReport(UUID userId, LocalDate startDate, LocalDate endDate) {
        WeeklyReport weeklyReport = weeklyReportRepository.findDailyReportBy(userId, startDate, endDate)
                .orElseThrow(() -> new WeeklyReportNotFoundException("주간분석이 존재하지 않습니다"));

        return WeeklyReportResponseDto.from(weeklyReport);
    }

    private ClovaResponseDto requestClovaAnalysis(List<Letter> letters) {
        String formattedMessages = letters.stream()
                .map(Letter::getMessage)
                .map(msg -> "\"" + msg + "\"")
                .collect(Collectors.joining(", "));

        return clovaService.sendDailyReportRequest(formattedMessages);
    }

    private DailyReport buildDailyReport(LocalDate targetDate, ClovaDailyAnalysisResult clovaDailyAnalysisResult) {
        return DailyReport.builder()
                .targetDate(targetDate)
                .coreEmotion(CoreEmotion.valueOf(clovaDailyAnalysisResult.getDailyCoreEmotion()))
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
                                    .map(CoreEmotion::valueOf)
                                    .collect(Collectors.toList()))
                            .sensitiveEmotions(analysis.getSensitiveEmotions())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<LocalDate> createOneWeek(LocalDate startDate) {
        return IntStream.range(0, 7)
                .mapToObj(startDate::plusDays)
                .toList();
    }
}
