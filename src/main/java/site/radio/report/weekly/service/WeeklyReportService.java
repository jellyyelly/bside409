package site.radio.report.weekly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.error.DailyReportNotFoundException;
import site.radio.error.WeeklyReportAlreadyExistsException;
import site.radio.error.WeeklyReportNotFoundException;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyStaticsOneWeekResponseDto;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.daily.service.DailyReportService;
import site.radio.report.weekly.domain.WeeklyReport;
import site.radio.report.weekly.dto.WeeklyReportResponseDto;
import site.radio.report.weekly.repository.WeeklyReportRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportPromptTemplate promptTemplate;
    private final DailyReportRepository dailyReportRepository;
    private final ClovaService clovaService;
    private final WeeklyReportRepository weeklyReportRepository;
    private final DailyReportService dailyReportService;

    /**
     * 분석 날짜, 작성빈도, 요일 ⛧ 감정 변화 추이, 위로 메세지, + 데일리의 각 편지 대표 감정 일요일 자정(00:00)을 넘으면 주간 분석 요청 버튼 활성화 및 주간 분석 요청 가능
     */
    public WeeklyReportResponseDto createWeeklyReport(UUID userId, LocalDate startDate) {
        // 주간 분석 생성할 수 있는지 검증
        if (weeklyReportRepository.fetchCountBy(userId, startDate, startDate.plusDays(6)).isPresent()) {
            throw new WeeklyReportAlreadyExistsException("주간 분석이 이미 존재합니다");
        }

        // 1주일 동안의 일일 분석 생성
        dailyReportService.createDailyReportsBy(userId, startDate, startDate.plusDays(6));

        // startDate 로 부터 1주일 날짜 구하기
        DailyStaticsOneWeekResponseDto dailyStaticsDto = dailyReportService.findDailyStaticsInOneWeek(userId,
                createOneWeek(startDate));

        // 1주일에 해당하는 일일 분석들 찾기
        List<DailyReport> dailyReports = dailyStaticsDto.getDailyReports();

        //dailyReport 에서 설명 합치기
        String descriptions = dailyReports.stream()
                .map(DailyReport::getDescription)
                .collect(Collectors.joining());

        // 주간 분석에 필요한 위로 한마디 요청
        CreateResponse createResponse = clovaService.sendWithPromptTemplate(promptTemplate, descriptions);
        String resultMessage = createResponse.getResultMessage();

        WeeklyDataManager manager = new WeeklyDataManager(startDate); // TODO: util class 로 리팩토링 필요
        WeeklyReport weeklyReport = WeeklyReport.builder()
                .weekOfYear(manager.getWeekOfWeekBasedYear())
                .startDate(manager.getMondayOfWeek())
                .endDate(manager.getSundayOfWeek())
                .cheerUp(resultMessage)
                .publishedCount(dailyStaticsDto.getStaticsDto().getPublishedCount())
                .unpublishedCount(dailyStaticsDto.getStaticsDto().getUnPublishedCount())
                .build();
        weeklyReportRepository.save(weeklyReport);
        dailyReports.forEach(dailyReport -> dailyReport.setWeeklyReport(weeklyReport));

        List<CoreEmotion> coreEmotions = dailyReports.stream()
                .map(DailyReport::getCoreEmotion)
                .toList();

        return WeeklyReportResponseDto.from(weeklyReport, coreEmotions);
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponseDto getWeeklyReport(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<DailyReport> dailyReports = dailyReportRepository.findDailyReportsWithWeeklyReport(userId, startDate,
                endDate);

        if (dailyReports.isEmpty()) {
            throw new DailyReportNotFoundException("데일리 리포트가 존재하지 않습니다");
        }

        List<CoreEmotion> coreEmotions = dailyReports.stream()
                .map(DailyReport::getCoreEmotion)
                .toList();

        WeeklyReport weeklyReport = dailyReports.stream()
                .findAny()
                .map(DailyReport::getWeeklyReport)
                .orElseThrow(() -> new WeeklyReportNotFoundException("위클리 리포트가 존재하지 않습니다"));

        return WeeklyReportResponseDto.from(weeklyReport, coreEmotions);
    }

    private List<LocalDate> createOneWeek(LocalDate startDate) {
        return IntStream.range(0, 7)
                .mapToObj(startDate::plusDays)
                .toList();
    }
}
