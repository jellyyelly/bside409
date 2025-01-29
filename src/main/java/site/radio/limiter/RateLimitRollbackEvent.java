package site.radio.limiter;

import lombok.Getter;
import site.radio.common.EventStatus;

@Getter
public class RateLimitRollbackEvent {

    private final String userId;
    private EventStatus status;

    public RateLimitRollbackEvent(String userId) {
        this.userId = userId;
        this.status = EventStatus.ROLLBACK_REQUIRED;
    }

    public static RateLimitRollbackEvent createEvent(String userId) {
        return new RateLimitRollbackEvent(userId);
    }

    public void process() {
        this.status = EventStatus.ROLLBACK_PROCESSING;
    }

    public void complete() {
        this.status = EventStatus.ROLLBACK_COMPLETED;
    }
}
