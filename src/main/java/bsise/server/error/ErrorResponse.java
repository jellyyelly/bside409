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
}