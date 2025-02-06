package site.radio.reply.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.error.LetterNotFoundException;
import site.radio.error.UserNotFoundException;
import site.radio.reply.domain.Letter;
import site.radio.reply.dto.DailyLetters;
import site.radio.reply.repository.LetterRepository;
import site.radio.user.domain.Preference;
import site.radio.user.domain.User;
import site.radio.user.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;

    public Letter save(UUID userId, String message, Preference preference, boolean published) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        Letter letter = Letter.builder()
                .user(user)
                .message(message)
                .preference(preference)
                .published(published)
                .build();

        return letterRepository.save(letter);
    }

    // FIXME: presentation layer 에 절대 나가지 않도록 개선하기
    @Transactional(readOnly = true)
    public Letter findLetter(UUID letterId) {
        return letterRepository.findById(letterId)
                .orElseThrow(() -> new LetterNotFoundException("Letter not found: " + letterId));
    }

    public void deleteLetter(UUID letterId) {
        letterRepository.deleteById(letterId);
    }

    @Transactional(readOnly = true)
    public DailyLetters findAnalyzableLetters(UUID userId, LocalDate targetDate) {
        List<Letter> analyzableLetters = letterRepository.findAnalyzableLetters(userId, toStartDateTime(targetDate),
                toEndDateTime(targetDate));

        return DailyLetters.from(analyzableLetters);
    }

    @Transactional(readOnly = true)
    public List<DailyLetters> findAnalyzableLettersInRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Letter> analyzableLetters = letterRepository.findAnalyzableLetters(userId, toStartDateTime(startDate),
                toEndDateTime(endDate));

        Map<LocalDate, List<Letter>> analyzableLettersByDate = analyzableLetters.stream()
                .collect(Collectors.groupingBy(letter -> letter.getCreatedAt().toLocalDate()));

        return analyzableLettersByDate.values().stream()
                .map(DailyLetters::from)
                .toList();
    }

    private LocalDateTime toStartDateTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    private LocalDateTime toEndDateTime(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX.minusNanos(999));
    }
}
