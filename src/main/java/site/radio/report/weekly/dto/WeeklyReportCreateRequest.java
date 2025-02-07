package site.radio.report.weekly.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "주간 분석 생성 DTO")
@Getter
@RequiredArgsConstructor
public class WeeklyReportCreateRequest {

    @Schema(description = "유저의 아이디", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "유저의 아이디가 존재하지 않습니다.")
    private final String userId;

    @Schema(description = "유저가 주간 분석을 받을 주차의 시작 날짜", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "주간 분석을 받을 주차의 시작 날짜를 입력해 주세요.")
    private final LocalDate startDate;
}
