package site.radio.common;

public enum EventStatus {

    // 기본 상태
    PENDING,
    PROCESSING,
    COMPLETED,

    // 롤백 상태
    ROLLBACK_REQUIRED,
    ROLLBACK_PROCESSING,
    ROLLBACK_COMPLETED,
}
