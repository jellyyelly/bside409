package site.radio.letter;

import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.common.EventStatus;
import site.radio.reply.ReplyResponseDto;
import site.radio.reply.TwoTypeMessage;
import site.radio.user.domain.Preference;

@Getter
@RequiredArgsConstructor
public class LetterCreationEvent {

    private final String userId;
    private final String message;
    private final Preference preference;
    private final boolean published;
    private final TwoTypeMessage twoTypeMessage;
    private final CompletableFuture<ReplyResponseDto> future;
    private EventStatus status = EventStatus.PENDING;

    public static LetterCreationEvent createEvent(String userId,
                                                  String message,
                                                  Preference preference,
                                                  boolean published,
                                                  TwoTypeMessage twoTypeMessage,
                                                  CompletableFuture<ReplyResponseDto> future) {
        return new LetterCreationEvent(userId, message, preference, published, twoTypeMessage, future);
    }

    public void process() {
        this.status = EventStatus.PROCESSING;
    }

    public void complete() {
        this.status = EventStatus.COMPLETED;
    }
}
