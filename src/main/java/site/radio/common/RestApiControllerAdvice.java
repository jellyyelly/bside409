package site.radio.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import site.radio.error.CustomErrorResponse;
import site.radio.error.DormantUserLoginException;
import site.radio.error.ExceptionType;
import site.radio.error.ExternalApiFallbackException;
import site.radio.error.ValidationErrorResponse;
import site.radio.error.ValidationProblemDetails;

@Slf4j
@RestControllerAdvice
public class RestApiControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleExceptions(Exception exception) {
        log.error("An error occurred: ", exception);
        return createErrorResponse(exception);
    }

    @ExceptionHandler(DormantUserLoginException.class)
    public ResponseEntity<?> handleDormantUserLoginError(DormantUserLoginException exception) {
        log.error("An error occurred: ", exception);
        return createErrorResponse(exception, exception.getMessage());
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<?> handleNoFallbackAvailableException(HttpServletRequest request,
                                                                NoFallbackAvailableException exception) {
        log.warn("error occurred uri: {}, exception: ", request.getRequestURI(), exception.getCause());
        return createErrorResponse(exception);
    }

    @ExceptionHandler(ExternalApiFallbackException.class)
    public ResponseEntity<?> handleExternalApiFallbackException(HttpServletRequest request,
                                                                ExternalApiFallbackException exception) {
        log.error("error occurred uri: {}, exception: ", request.getRequestURI(), exception);
        return createErrorResponse(exception);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("An error occurred: ", exception);

        List<ValidationProblemDetails> problemDetails = exception.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationProblemDetails(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )).toList();

        return ResponseEntity.status(status)
                .body(ValidationErrorResponse.of(ExceptionType.from(exception), problemDetails));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode,
            WebRequest request) {
        log.error("An error occurred: ", exception);

        return ResponseEntity.status(statusCode)
                .body(CustomErrorResponse.of(ExceptionType.from(exception), null));
    }

    private ResponseEntity<?> createErrorResponse(Exception exception) {
        return createErrorResponse(exception, null);
    }

    private ResponseEntity<?> createErrorResponse(Exception exception, @Nullable String customMessage) {
        ExceptionType exceptionType = ExceptionType.from(exception);

        // 메시지가 제공되면 사용, 없으면 기본 메시지로 처리
        String message = (customMessage != null) ? customMessage : exception.getMessage();

        CustomErrorResponse errorResponse = CustomErrorResponse.of(exceptionType, message);
        return ResponseEntity.status(exceptionType.getHttpStatus()).body(errorResponse);
    }
}
