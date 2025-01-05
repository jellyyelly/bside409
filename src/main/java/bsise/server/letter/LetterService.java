package bsise.server.letter;

import bsise.server.error.LetterNotFoundException;
import bsise.server.error.RateLimitException;
import bsise.server.error.UserNotFoundException;
import bsise.server.limiter.RateLimitService;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
    public List<FulltextSearchResult> fulltextSearchWithNaturalLanguageMode(UUID userId, String searchText) {
        PageRequest page = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

        return letterRepository.fulltextSearchWithNaturalLanguageMode(userId, searchText, page);
    }

    @Transactional(readOnly = true)
    public List<FulltextSearchResult> fulltextSearchWithBooleanMode(UUID userId, String searchText) {
        PageRequest page = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

        return letterRepository.fulltextSearchWithBooleanMode(userId, searchText, page);
    }

    @Transactional(readOnly = true)
    public List<FulltextSearchResult> fulltextSearchWithQueryExpansion(UUID userId, String searchText) {
        PageRequest page = PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"));

        return letterRepository.fulltextSearchWithQueryExpansion(userId, searchText, page);
    }
}
