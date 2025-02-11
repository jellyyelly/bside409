package site.radio.limiter;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "유저의 사용량 정보를 포함하는 DTO")
@Getter
@AllArgsConstructor
public class UserUsageResponse {

    @Schema(description = "유저의 UUID")
    private UUID userId;

    @Schema(description = "현재까지 편지 작성한 횟수")
    private String usage;

    @Schema(description = "초기화까지 남은 시간")
    private Long ttl;

    public static UserUsageResponse of(String userId, String usage, Long ttl) {
        return new UserUsageResponse(UUID.fromString(userId), usage, ttl);
    }
}
