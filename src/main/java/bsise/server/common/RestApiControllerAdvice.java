package bsise.server.common;

import bsise.server.limiter.RateLimitException;
import jakarta.persistence.EntityNotFoundException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestApiControllerAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException exception) {
        return createErrorResponse(exception, HttpStatus.UNAUTHORIZED, "error.unauthorized");
    }

    @ExceptionHandler
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException exception) {
        return createErrorResponse(exception, HttpStatus.NOT_FOUND, "error.entity.not.found");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException exception) {
        return createErrorResponse(exception, HttpStatus.BAD_REQUEST, "error.illegal.argument");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException exception) {
        return createErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, "error.illegal.state");
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<?> handleRateLimitException(RateLimitException exception) {
        return createErrorResponse(exception, HttpStatus.TOO_MANY_REQUESTS, "error.rate.limit");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRateLimitException(RuntimeException exception) {
        return createErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, "error.rate.limit");
    }

    private ResponseEntity<?> createErrorResponse(Exception exception, HttpStatus status, String messageKey) {
        log.error("An error occurred: ", exception);
        String message = messageSource.getMessage(messageKey, null, Locale.KOREAN);
        ErrorResponse errorResponse = ErrorResponse.builder(exception,
                ProblemDetail.forStatusAndDetail(status, message)).build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
