package site.radio.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
import site.radio.user.domain.Preference;

@Schema(description = "유저 정보 변경 요청 DTO")
@Getter
@AllArgsConstructor
public class UserChangeRequest {

    @Schema(description = "변경을 희망하는 닉네임")
    @Length(min = 1, max = 12)
    @NotNull
    private String nickname;

    @Schema(description = "선호하는 답변 타입", allowableValues = {"T", "F"})
    @NotNull
    private Preference preference;

    @Schema(description = "카카오 프로필 이미지 동기화 여부", allowableValues = {"true", "false"})
    @NotNull
    private boolean profileImageEnabled;

    @Schema(description = "광고 이메일 수신 여부", allowableValues = {"true", "false"})
    @NotNull
    private boolean emailAdsConsented;

    @Schema(description = "이용 약관 동의 여부", allowableValues = {"true", "false"})
    @NotNull
    private boolean agreeToTerms;

    @Schema(description = "개인정보 처리 방침 동의 여부", allowableValues = {"true", "false"})
    @NotNull
    private boolean agreeToPrivacyPolicy;
}
