package site.radio.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Range;
import org.springframework.util.StringUtils;
import site.radio.user.domain.WithdrawalReason;

@Schema(description = "회원 탈퇴 요청 DTO")
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDeleteRequest {

    @Schema(description = "유저가 선택한 번호. "
                          + "0: 기타, 1: 자주 사용하지 않아요, 2: 개인 정보가 걱정돼요, 3: 사용하기가 불편해요, 4:답장이 마음에 들지 않아요",
            allowableValues = {"0", "1", "2", "3", "4"},
            example = "selectedNumber: 1 => '자주 사용하지 않아요.' 로 처리")
    @Range(min = 0, max = 4)
    private int selectedNumber;

    @Schema(description = "유저가 입력한 상세 탈퇴 사유. 입력이 없으면 해당 필드를 포함시키지 않는다.", requiredMode = RequiredMode.NOT_REQUIRED)
    private String detailReason;

    @Schema(hidden = true)
    public WithdrawalReason getWithdrawalReason() {
        return WithdrawalReason.of(selectedNumber);
    }

    public Optional<String> getDetailReason() {
        return StringUtils.hasText(detailReason) ? Optional.of(detailReason) : Optional.empty();
    }
}
