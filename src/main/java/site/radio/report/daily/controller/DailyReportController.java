package site.radio.report.daily.controller;

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
import site.radio.report.daily.dto.DailyReportCreateRequest;
import site.radio.report.daily.dto.DailyReportResponse;
import site.radio.report.daily.service.DailyReportService;

@RestController
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @PostMapping("/api/v1/reports/daily")
    @ResponseStatus(HttpStatus.CREATED)
    public DailyReportResponse createDailyReport(@Valid @RequestBody DailyReportCreateRequest dto) {
        return dailyReportService.createDailyReport(UUID.fromString(dto.getUserId()), dto.getTargetDate());
    }

    @GetMapping("/api/v1/reports/daily/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public DailyReportResponse getDailyReport(
            @PathVariable("userId") UUID userId, @RequestParam("targetDate") LocalDate targetDate
    ) {
        return dailyReportService.getDailyReport(userId, targetDate);
    }
}
