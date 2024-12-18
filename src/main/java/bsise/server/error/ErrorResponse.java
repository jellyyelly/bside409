package bsise.server.error;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {
    private final Instant timestamp;
    private final int status;
    private final String title;
    private final String key;
    private final String message;

    public static ErrorResponse of(ErrorType errorType, String message) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .title(errorType.getHttpStatus().getReasonPhrase())
                .status(errorType.getHttpStatus().value())
                .key(errorType.getMessageKey())
                .message(message)
                .build();
    }
}