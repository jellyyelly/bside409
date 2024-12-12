package bsise.server.report.monthly.controller;

import bsise.server.report.monthly.dto.MonthlyReportResultResponseDto;
import bsise.server.report.monthly.service.MonthlyReportResultService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
public class MonthlyReportResultController {

    private final MonthlyReportResultService monthlyReportResultService;

    @GetMapping("/api/v1/reports/monthly/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public MonthlyReportResultResponseDto getMonthlyReportResult(
            @PathVariable String userId,
            @RequestParam @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "연-월 요청 포맷은 yyyy-MM 형식이어야 합니다.") String yearMonth) {
        return monthlyReportResultService.getMonthlyReportResult(UUID.fromString(userId), yearMonth);
    }
}
