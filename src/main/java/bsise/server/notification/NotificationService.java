package bsise.server.notification;

import bsise.server.error.UserNotFoundException;
import bsise.server.letter.LetterRepository;
import bsise.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final SseEmitters sseEmitters;
    private final UserRepository userRepository;
    private final LetterRepository letterRepository;

    public SseEmitter subscribe(String userId) {
        if(!userRepository.existsUserById(UUID.fromString(userId))) {
            throw new UserNotFoundException("User not found : " + userId);
        }

        return sseEmitters.addEmitter(userId);
    }

    // 매일 오후 9시에 실행
    @Scheduled(cron = "0 0 21 * * ?")
    public void sendDailyNotifications() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        String notificationMessage = "오늘의 일일 리포트를 생성해 보세요!";

        sseEmitters.getActiveUserIds().stream()
                .filter(userId -> letterRepository.existsByUserIdAndCreatedAtBetween(UUID.fromString(userId), startOfDay, now))
                .forEach(userId -> sseEmitters.sendToUser(userId, notificationMessage));
    }
}
