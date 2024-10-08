package bsise.server.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "유저 정보 응답 DTO")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserResponseDto {

    @Schema(description = "유저 아이디", example = "123e4567-e89b-12d3-a456-426655440000")
    private String userId;

    @Schema(description = "유저 닉네임", examples = {"임시 닉네임", "변경 후 유저 닉네임"})
    private String nickname;

    @Schema(description = "이메일", example = "kakao@email.com")
    private String email;

    @Schema(description = "선호하는 답변 타입", examples = {"T", "F"})
    private Preference preference;

    @Schema(description = "이미지 동기화 여부", examples = {"true", "false"})
    private boolean profileImageDisable;

    @Schema(description = "최초 로그인 여부", examples = {"true", "false"})
    private boolean isFirstLogin;

    public static UserResponseDto of(User user) {
        return UserResponseDto.builder()
                .userId(user.getId().toString())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .preference(user.getPreference())
                .profileImageDisable(user.isSynced())
                .isFirstLogin(user.getNickname().equals("임시 닉네임"))
                .build();
    }
}
