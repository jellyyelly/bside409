package bsise.server.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    // 400
    ERROR_ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "error.illegal.argument"),
    ERROR_ARGS_INVALID(HttpStatus.BAD_REQUEST, "error.args.invalid"),

    // 401
    ERROR_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "error.unauthorized"),

    // 404
    ERROR_ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "error.entity.not.found"),

    // 409
    ERROR_DORMANT_USER_LOGIN(HttpStatus.CONFLICT, "error.dormantuser.login"),
    ERROR_NAMED_LOCK(HttpStatus.CONFLICT, "error.namedLock"),
    ERROR_DUPLICATE(HttpStatus.CONFLICT, "error.duplicate"),

    // 429
    ERROR_RATE_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "error.rate.limit"),

    // 500
    ERROR_ILLEGAL_STATE(HttpStatus.INTERNAL_SERVER_ERROR, "error.illegal.state"),
    ERROR_UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR, "error.unknown");   // default

    private final HttpStatus httpStatus;
    private final String messageKey;
}
