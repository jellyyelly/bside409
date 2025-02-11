package site.radio.report.weekly.service;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.radio.report.daily.service.DailyReportService;
import site.radio.report.daily.service.LetterAnalysisService;
import site.radio.report.weekly.domain.WeeklyReport;
import site.radio.report.weekly.dto.WeeklyLetterAnalyses;
import site.radio.report.weekly.dto.WeeklyReportResponse;

@Service
@RequiredArgsConstructor
public class WeeklyReportServiceFacade {

    private final LetterAnalysisService letterAnalysisService;
    private final DailyReportService dailyReportService;
    private final WeeklyReportService weeklyReportService;

    public WeeklyReportResponse createWeeklyReport(UUID userId, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        // 주간 분석 생성할 수 있는지 검증
        weeklyReportService.validateNotExistsWeeklyReport(userId, startDate);

        // 1주간 데일리 리포트 미리 생성
        dailyReportService.preCreateDailyReport(userId, startDate, endDate);

        // 주간 편지 분석 DTO 조회
        WeeklyLetterAnalyses analyses = letterAnalysisService.findLetterAnalysesInRange(userId, startDate, endDate);

        // 위클리 리포트 응원 메시지 생성 요청
        String cheerUp = weeklyReportService.createCheerUpMessage(analyses);

        // 트랜잭션 안에서 저장
        WeeklyReport weeklyReport = weeklyReportService.save(analyses, cheerUp);

        return WeeklyReportResponse.from(weeklyReport, analyses.getCoreEmotions());
    }
}
