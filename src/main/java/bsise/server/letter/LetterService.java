package bsise.server.letter;

import bsise.server.error.LetterNotFoundException;
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
