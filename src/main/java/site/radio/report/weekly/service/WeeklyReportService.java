package site.radio.report.weekly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.clova.dto.CreateResponse;
import site.radio.clova.service.ClovaService;
import site.radio.error.WeeklyReportAlreadyExistsException;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.util.CustomDateUtils;
import site.radio.report.weekly.domain.WeeklyReport;
import site.radio.report.weekly.dto.WeeklyLetterAnalyses;
import site.radio.report.weekly.dto.WeeklyReportProjection;
import site.radio.report.weekly.dto.WeeklyReportResponse;
import site.radio.report.weekly.repository.WeeklyReportRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportPromptTemplate promptTemplate;
    private final ClovaService clovaService;
    private final WeeklyReportRepository weeklyReportRepository;
    private final DailyReportRepository dailyReportRepository;


    /**
     * 주간 편지 분석에 따른 한주 요약 메시지 생성을 담당합니다.
     *
     * @param weeklyLetterAnalyses 주간 편지 분석
     * @return 생성된 한주 요약 메시지
     */
    public String createCheerUpMessage(WeeklyLetterAnalyses weeklyLetterAnalyses) {
        // 위클리 리포트 생성 요청
        CreateResponse createResponse = clovaService.sendWithPromptTemplate(promptTemplate,
                weeklyLetterAnalyses.getMergedDescription());

        // 응답 추출
        return createResponse.getResultMessage();
    }

    @Transactional
    public WeeklyReport save(WeeklyLetterAnalyses weeklyLetterAnalyses, String cheerUpMessage) {
        // 위클리 리포트 persist
        LocalDate startDate = weeklyLetterAnalyses.getStartDate();
        LocalDate endDate = weeklyLetterAnalyses.getEndDate();

        WeeklyReport weeklyReport = WeeklyReport.builder()
                .weekOfYear(CustomDateUtils.getWeekOfWeekBasedYear(startDate))
                .startDate(startDate)
                .endDate(endDate)
                .cheerUp(cheerUpMessage)
                .publishedCount(weeklyLetterAnalyses.getPublishedCount())
                .unpublishedCount(weeklyLetterAnalyses.getUnpublishedCount())
                .build();

        weeklyReport = weeklyReportRepository.saveAndFlush(weeklyReport);

        // 연관 관계 매핑
        List<DailyReport> dailyReports = weeklyLetterAnalyses.getDailyReports();
        List<UUID> dailyReportIds = dailyReports.stream()
                .map(DailyReport::getId)
                .toList();

        dailyReportRepository.bulkUpdateWeeklyReport(weeklyReport, dailyReportIds);

        return weeklyReport;
    }

    @Transactional(readOnly = true)
    public void validateNotExistsWeeklyReport(UUID userId, LocalDate startDate) {
        if (weeklyReportRepository.fetchCountBy(userId, startDate, startDate.plusDays(6)).isPresent()) {
            throw new WeeklyReportAlreadyExistsException("주간 분석이 이미 존재합니다");
        }
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse getWeeklyReport(UUID userId, LocalDate startDate, LocalDate endDate) {
        WeeklyReportProjection projection = weeklyReportRepository.findWeeklyReportDtoBy(userId, startDate,
                endDate);

        return WeeklyReportResponse.from(projection);
    }
}
