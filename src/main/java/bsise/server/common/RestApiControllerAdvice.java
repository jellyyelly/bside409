package bsise.server.common;

import bsise.server.error.DormantUserLoginException;
import bsise.server.error.DuplicateException;
import bsise.server.error.NamedLockAcquisitionException;
import bsise.server.error.RateLimitException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestApiControllerAdvice {

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

    @ExceptionHandler(DormantUserLoginException.class)
    public ResponseEntity<?> handleDormantUserLoginError(DormantUserLoginException exception) {
        return createErrorResponse(exception, HttpStatus.CONFLICT, "error.dormantuser.login:" + exception.getMessage());
    }

    @ExceptionHandler(NamedLockAcquisitionException.class)
    public ResponseEntity<?> handleNamedLockAcquisitionException(NamedLockAcquisitionException exception) {
        return createErrorResponse(exception, HttpStatus.CONFLICT, "error.namedLock:" + exception.getMessage());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<?> handleDuplicateException(DuplicateException exception) {
        return createErrorResponse(exception, HttpStatus.CONFLICT, "error.duplicate: " + exception.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<String> errorMessages = exception.getBindingResult().getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
        return createErrorResponse(exception, HttpStatus.BAD_REQUEST, "error.args.invalid: " + errorMessages);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException exception) {
        return createErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, "error.unknown");
    }

    private ResponseEntity<?> createErrorResponse(Exception exception, HttpStatus status, String messageKey) {
        log.error("An error occurred: ", exception);
        ErrorResponse errorResponse = ErrorResponse.builder(exception,
                ProblemDetail.forStatusAndDetail(status, messageKey)).build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
