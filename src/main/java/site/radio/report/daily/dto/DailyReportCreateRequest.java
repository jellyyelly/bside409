package site.radio.report.daily.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DailyReportCreateRequest {

    @NotBlank(message = "유저의 아이디가 존재하지 않습니다.")
    private String userId;

    @NotNull(message = "생성 요청 날짜는 필수 요청 값입니다.")
    private LocalDate targetDate;
}
