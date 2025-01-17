package site.radio.letter;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.radio.error.LetterNotFoundException;
import site.radio.error.RateLimitException;
import site.radio.error.UserNotFoundException;
import site.radio.limiter.RateLimitService;
import site.radio.user.domain.Preference;
import site.radio.user.domain.User;
import site.radio.user.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;
    private final RateLimitService rateLimitService;

    public LetterResponseDto saveLetter(LetterRequestDto letterDto) {
        if (!rateLimitService.preDeductUsage(letterDto.getUserId())) {
            throw new RateLimitException("요청 제한 횟수 초과");
        }
        User user = userRepository.findById(UUID.fromString(letterDto.getUserId()))
                .orElseThrow(() -> new UserNotFoundException("User not found: " + letterDto.getUserId()));

        Letter letter = letterDto.toLetterWithoutUser();
        letter.setUser(user);

        Letter savedLetter = letterRepository.save(letter);

        return LetterResponseDto.fromLetter(savedLetter);
    }

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
    public List<LetterResponseDto> getLatestLetters() {
        List<Letter> top10Letters = letterRepository.findTop10ByPublishedIsTrueOrderByCreatedAtDesc();

        return top10Letters.stream()
                .map(LetterResponseDto::fromLetter)
                .toList();
    }
}
