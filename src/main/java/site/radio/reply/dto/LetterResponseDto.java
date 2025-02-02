package site.radio.reply.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.reply.domain.Letter;
import site.radio.user.domain.Preference;

@Tag(name = "letter")
@Schema(description = "유저가 전송한 편지가 저장에 성공하면 받는 응답 DTO")
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class LetterResponseDto {

    @JsonIgnore
    private UUID userId;

    @Schema(description = "저장된 편지의 식별자")
    private UUID letterId;

    @Schema(description = "유저가 선호하는 답장 유형", examples = {"F", "T"})
    private Preference preference;

    @Schema(description = "편지 내용")
    private String content;

    @Schema(description = "공개 여부")
    private boolean published;

    @Schema(description = "편지가 저장된 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static LetterResponseDto fromLetter(Letter letter) {
        return LetterResponseDto.builder()
                .userId(letter.getUser().getId())
                .letterId(letter.getId())
                .preference(letter.getPreference())
                .content(letter.getMessage())
                .published(letter.isPublished())
                .createdAt(letter.getCreatedAt())
                .build();
    }
}
