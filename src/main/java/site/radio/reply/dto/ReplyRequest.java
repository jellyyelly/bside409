package site.radio.reply.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import site.radio.reply.domain.Letter;
import site.radio.user.domain.Preference;

@Schema(description = "유저가 전송하는 편지 DTO")
@Getter
public class ReplyRequest {

    @Schema(description = "유저의 아이디", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "유저의 아이디가 존재하지 않습니다.")
    private final String userId;

    @Schema(description = "유저가 작성한 메시지 내용", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "유저가 작성한 메시지가 없습니다.")
    private final String message;

    @Schema(description = "유저가 선호하는 답변 유형", allowableValues = {"F", "T"}, requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "선호하는 답변 유형이 없습니다.")
    private final Preference preference;

    @Schema(description = "편지 공개 여부", allowableValues = {"true", "false"}, requiredMode = RequiredMode.REQUIRED)
    private final boolean published;

    private final LocalDate targetDate;

    @JsonCreator
    public ReplyRequest(
            @JsonProperty("userId") String userId,
            @JsonProperty("message") String message,
            @JsonProperty("preference") Preference preference,
            @JsonProperty("published") boolean published
    ) {
        this.userId = userId;
        this.message = message;
        this.preference = preference;
        this.published = published;
        this.targetDate = LocalDate.now(); // 역직렬화 시 자동으로 현재 날짜 설정
    }

    public Letter toLetterWithoutUser() {
        return Letter.builder()
                .message(message)
                .preference(preference)
                .published(published)
                .build();
    }
}
