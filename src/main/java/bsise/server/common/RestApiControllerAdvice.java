package bsise.server.common;

import bsise.server.error.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

import static bsise.server.error.ErrorType.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestApiControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException exception) {
        return createErrorResponse(exception, ERROR_UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException exception) {
        return createErrorResponse(exception, ERROR_ENTITY_NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException exception) {
        return createErrorResponse(exception, ERROR_ILLEGAL_ARGUMENT);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException exception) {
        return createErrorResponse(exception, ERROR_ILLEGAL_STATE);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<?> handleRateLimitException(RateLimitException exception) {
        return createErrorResponse(exception, ERROR_RATE_LIMIT);
    }

    @ExceptionHandler(DormantUserLoginException.class)
    public ResponseEntity<?> handleDormantUserLoginError(DormantUserLoginException exception) {
        return createErrorResponse(exception, ERROR_DORMANT_USER_LOGIN, exception.getMessage());
    }

    @ExceptionHandler(NamedLockAcquisitionException.class)
    public ResponseEntity<?> handleNamedLockAcquisitionException(NamedLockAcquisitionException exception) {
        return createErrorResponse(exception, ERROR_NAMED_LOCK, exception.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<?> handleDuplicateException(DuplicateException exception) {
        return createErrorResponse(exception, ERROR_DUPLICATE, exception.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return createErrorResponse(exception, ERROR_ARGS_INVALID, errorMessages.toString());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException exception) {
        return createErrorResponse(exception, ERROR_UNKNOWN);
    }

    private ResponseEntity<?> createErrorResponse(Throwable throwable, ErrorType errorType, String... message) {
        log.error("An error occurred: ", throwable);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .title(errorType.getHttpStatus().getReasonPhrase())
                .status(errorType.getHttpStatus().value())
                .key(errorType.getMessageKey())
                .message(message != null && message.length > 0 && message[0] != null ? message[0] : null)
                .build();

        return ResponseEntity.status(errorType.getHttpStatus()).body(errorResponse);
    }
}
