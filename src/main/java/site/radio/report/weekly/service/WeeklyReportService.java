package site.radio.report.weekly.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.error.DailyReportNotFoundException;
import site.radio.error.WeeklyReportAlreadyExistsException;
import site.radio.error.WeeklyReportNotFoundException;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.dto.DailyReportStaticsDto;
import site.radio.report.daily.dto.DailyStaticsOneWeekResponseDto;
import site.radio.report.daily.repository.DailyReportRepository;
import site.radio.report.weekly.domain.WeeklyReport;
import site.radio.report.weekly.dto.WeeklyReportResponseDto;
import site.radio.report.weekly.repository.WeeklyReportRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class WeeklyReportService {

    private final DailyReportRepository dailyReportRepository;
    private final WeeklyReportRepository weeklyReportRepository;

    /**
     * 분석 날짜, 작성빈도, 요일 ⛧ 감정 변화 추이, 위로 메세지, + 데일리의 각 편지 대표 감정 일요일 자정(00:00)을 넘으면 주간 분석 요청 버튼 활성화 및 주간 분석 요청 가능
     */
    public WeeklyReportResponseDto createWeeklyReport(UUID userId, List<LocalDate> oneWeekDates, String resultMessage) {
        List<DailyReport> dailyReports = dailyReportRepository.findByTargetDateIn(userId, oneWeekDates);
        DailyReportStaticsDto dto = dailyReportRepository.findStaticsBy(userId, oneWeekDates);

        int startDateIndex = 0;
        WeeklyDataManager manager = new WeeklyDataManager(oneWeekDates.get(startDateIndex)); // TODO: util class 로 리팩토링 필요
        WeeklyReport weeklyReport = WeeklyReport.builder()
                .weekOfYear(manager.getWeekOfWeekBasedYear())
                .startDate(manager.getMondayOfWeek())
                .endDate(manager.getSundayOfWeek())
                .cheerUp(resultMessage)
                .publishedCount(dto.getPublishedCount())
                .unpublishedCount(dto.getUnPublishedCount())
                .build();
        weeklyReportRepository.save(weeklyReport);
        dailyReports.forEach(dailyReport -> dailyReport.setWeeklyReport(weeklyReport));

        List<CoreEmotion> coreEmotions = dailyReports.stream()
                .map(DailyReport::getCoreEmotion)
                .toList();

        return WeeklyReportResponseDto.from(weeklyReport, coreEmotions);
    }

    public WeeklyReportResponseDto createWeeklyReportV2(DailyStaticsOneWeekResponseDto dailyStaticsOneWeekDto, String resultMessage, LocalDate startDate) {
        DailyReportStaticsDto staticsDto = dailyStaticsOneWeekDto.getStaticsDto();

        // 주간 리포트 생성
        WeeklyDataManager manager = new WeeklyDataManager(startDate); // TODO: util class 로 리팩토링 필요
        WeeklyReport weeklyReport = WeeklyReport.builder()
                .weekOfYear(manager.getWeekOfWeekBasedYear())
                .startDate(manager.getMondayOfWeek())
                .endDate(manager.getSundayOfWeek())
                .cheerUp(resultMessage)
                .publishedCount(staticsDto.getPublishedCount())
                .unpublishedCount(staticsDto.getUnPublishedCount())
                .build();
        weeklyReportRepository.save(weeklyReport);

        // DailyReport 는 준영속 상태
        List<DailyReport> dailyReports = dailyStaticsOneWeekDto.getDailyReports();
        List<UUID> dailyReportIds = dailyReports.stream()
                .map(DailyReport::getId)
                .collect(Collectors.toList());

        // UPDATE 쿼리
        dailyReportRepository.updateWeeklyReportId(weeklyReport, dailyReportIds);

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

    @Transactional(readOnly = true)
    public void vaildateWeeklyReportBy(UUID userId, LocalDate startDate) {
        // 주간 분석 생성할 수 있는지 검증
        if (weeklyReportRepository.fetchCountBy(userId, startDate, startDate.plusDays(6)).isPresent()) {
            throw new WeeklyReportAlreadyExistsException("주간 분석이 이미 존재합니다");
        }
    }
}
