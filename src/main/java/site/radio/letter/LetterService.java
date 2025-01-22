package site.radio.letter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import site.radio.error.LetterNotFoundException;
import site.radio.error.RateLimitException;
import site.radio.error.UserNotFoundException;
import site.radio.limiter.RateLimitService;
import site.radio.user.domain.User;
import site.radio.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    public LetterResponseDto saveLetter(LetterRequestDto letterDto) {
        if (!rateLimitService.isRequestAllowed(letterDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }
        User user = userRepository.findById(UUID.fromString(letterDto.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + letterDto.getUserId()));

        Letter letter = letterDto.toLetterWithoutUser();
        letter.setUser(user);

        Letter savedLetter = letterRepository.save(letter);

        return LetterResponseDto.fromLetter(savedLetter);
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
    public List<LetterResponseDto> getLatestLetters() {
        List<Letter> top10Letters = letterRepository.findTop10ByPublishedIsTrueOrderByCreatedAtDesc();

        return top10Letters.stream()
                .map(LetterResponseDto::fromLetter)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, List<Letter>> findLettersForDailyReport(UUID userId, LocalDate startDate, LocalDate endDate) {
        // 주간분석을 요청한 기간 동안 사용자가 작성한 편지들 찾기
        List<Letter> userLettersByLatest = letterRepository.findByCreatedAtDesc(userId,
                startDate.atStartOfDay(),
                LocalDateTime.of(endDate, LocalTime.MAX));

        // 날짜별로 편지들을 3개씩 묶기
        Map<LocalDate, List<Letter>> latestLettersByDate = userLettersByLatest.stream()
                .collect(Collectors.groupingBy(
                        letter -> letter.getCreatedAt().toLocalDate()));

        // 이미 일일 분석이 생성된 날짜는 제거
        latestLettersByDate.values().removeIf(
                letters -> letters.stream().anyMatch(letter -> letter.getDailyReport() != null)
        );

        // 일일 분석을 생성하려는 편지들을 날짜당 3개로 제한
        return latestLettersByDate.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().stream()
                                .limit(3)
                                .collect(Collectors.toList())
                ));
    }
}
