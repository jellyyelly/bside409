package site.radio.reply.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.reply.domain.Reply;

@Schema(description = "CLOVA가 생성한 두 유형의 답장 DTO")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class TwoTypeMessage {

    @Schema(description = "F 유형의 답장")
    @JsonProperty(value = "message_f")
    private final String messageForF;

    @Schema(description = "T 유형의 답장")
    @JsonProperty(value = "message_t")
    private final String messageForT;

    public static TwoTypeMessage of(String messageForF, String messageForT) {
        return new TwoTypeMessage(messageForF, messageForT);
    }

    public static TwoTypeMessage fromReply(Reply reply) {
        return new TwoTypeMessage(reply.getMessageForF(), reply.getMessageForT());
    }
}
