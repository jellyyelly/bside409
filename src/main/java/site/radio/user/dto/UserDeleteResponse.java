package site.radio.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import site.radio.user.domain.User;

@Schema(description = "회원 탈퇴 응답 DTO")
@Getter
@AllArgsConstructor
public class UserDeleteResponse {

    @Schema(description = "탈퇴한 유저 아이디", example = "123e4567-e89b-12d3-a456-426655440000")
    private UUID userId;

    public static UserDeleteResponse of(User user) {
        return new UserDeleteResponse(user.getId());
    }
}
