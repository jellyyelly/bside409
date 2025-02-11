package site.radio.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.user.domain.Preference;
import site.radio.user.domain.User;

@Schema(description = "유저 정보 응답 DTO")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponse {

    @Schema(description = "유저 아이디", example = "123e4567-e89b-12d3-a456-426655440000")
    private String userId;

    @Schema(description = "유저 닉네임", examples = {"임시 닉네임", "변경 후 유저 닉네임"})
    private String nickname;

    @Schema(description = "이메일", example = "kakao@email.com")
    private String email;

    @Schema(description = "선호하는 답변 타입", examples = {"T", "F"})
    private Preference preference;

    @Schema(description = "이미지 동기화 여부", examples = {"true", "false"})
    private boolean profileImageEnabled;

    @Schema(description = "광고 이메일 수신 여부", examples = {"true", "false"})
    private boolean emailAdsConsented;

    @Schema(description = "이용 약관 동의 여부", examples = {"true", "false"})
    private boolean agreeToTerms;

    @Schema(description = "개인정보 처리 방침 동의 여부", examples = {"true", "false"})
    private boolean agreeToPrivacyPolicy;

    @Schema(description = "최초 로그인 여부", examples = {"true", "false"})
    private boolean isFirstLogin;

    public static UserResponse of(User user) {
        return UserResponse.builder()
                .userId(user.getId().toString())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .preference(user.getPreference())
                .profileImageEnabled(user.isSynced())
                .emailAdsConsented(user.isEmailAdsConsented())
                .isFirstLogin(user.getNickname().equals("임시 닉네임"))
                .agreeToTerms(user.isAgreeToTerms())
                .agreeToPrivacyPolicy(user.isAgreeToPrivacyPolicy())
                .build();
    }
}
