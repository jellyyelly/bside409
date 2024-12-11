package bsise.server.notification;

import bsise.server.error.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitters {

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * 특정 사용자에 대한 SseEmitter를 추가합니다.
     * @param userId 사용자 아이디
     * @return 추가한 SseEmitter
     */
    // TODO: timeout 설정 및 필요한 로그만 남기기
    // TODO: user 필드에 isNotificationTurnedOn 추가, 알림 설정여부 체크 로직 추가
    public SseEmitter addEmitter(String userId) {
        SseEmitter emitter = new SseEmitter();

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(exception -> emitters.remove(userId));

        emitters.put(userId, emitter);
        log.debug("Adding emitter {} to user {}. Emitter list size : {}", emitter, userId, emitters.size());

        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("Connection established. - Notification subscription"));
        } catch (IOException e) {
            throw new NotificationException(String.format("Connection establishment with %s failed", userId));
        }

        return emitter;
    }

    /**
     * 현재 접속 중인 사용자 목록을 반환합니다.
     * @return 현재 접속 중인 사용자 ID 목록
     */
    public Set<String> getActiveUserIds() {
        return emitters.keySet();
    }

    /**
     * 알림을 설정한 특정 사용자에게 메시지를 전송합니다.
     * @param userId 사용자 아이디
     * @param message 알림 메시지
     */
    public void sendToUser(String userId, String message) {
        try {
            emitters.get(userId).send(message);
        } catch (IOException e) {
            throw new NotificationException(String.format("Sending message to %s failed", userId));
        }
    }

    /**
     * 알림을 설정한 모든 사용자에게 메시지를 전송합니다.
     * @param message 전체 알림 메시지
     */
    public void broadcast(String message) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(message);
            } catch (IOException e) {
                throw new NotificationException(String.format("Sending message to %s failed", userId));
            }
        });
    }
}
