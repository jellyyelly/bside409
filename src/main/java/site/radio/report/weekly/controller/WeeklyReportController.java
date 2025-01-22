package site.radio.report.weekly.controller;

import site.radio.report.weekly.dto.WeeklyReportRequestDto;
import site.radio.report.weekly.dto.WeeklyReportResponseDto;
import site.radio.report.weekly.service.WeeklyFacade;
import site.radio.report.weekly.service.WeeklyReportService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final WeeklyFacade weeklyFacade;

    @PostMapping("/api/v1/reports/weekly")
    @ResponseStatus(HttpStatus.CREATED)
    public WeeklyReportResponseDto createWeeklyReport(@Valid @RequestBody WeeklyReportRequestDto dto) {
//        return weeklyReportService.createWeeklyReport(UUID.fromString(dto.getUserId()), dto.getStartDate());
        return weeklyFacade.createWeeklyReport(UUID.fromString(dto.getUserId()), dto.getStartDate());
    }


    @GetMapping("/api/v1/reports/weekly/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public WeeklyReportResponseDto getWeeklyReport(
            @PathVariable("userId") UUID userId,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        return weeklyReportService.getWeeklyReport(userId, startDate, endDate);
    }
}
