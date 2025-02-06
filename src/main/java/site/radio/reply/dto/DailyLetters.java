package site.radio.reply.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import site.radio.error.LetterNotFoundException;
import site.radio.reply.domain.Letter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyLetters {

    private static final String MERGE_FORMAT = "<%s:%s>\n%s\n</%s:%s>";
    private static final String LETTER_SEPARATOR = "sharpie-sep";
    private static final String ESCAPE_LT = "<";
    private static final String ESCAPE_GT = ">";
    private static final String ESCAPE_AMP = "&";
    private static final String ESCAPE_QUOT = "\"";
    private static final String ESCAPE_APOS = "'";

    private List<Letter> letters;

    public static DailyLetters from(List<Letter> letters) {
        return new DailyLetters(letters);
    }

    public Letter getLetter(int naturalIndex) {
        return letters.get(naturalIndex - 1);
    }

    public LocalDate getCreatedDate() {
        return letters.stream()
                .findAny()
                .orElseThrow(() -> new LetterNotFoundException("편지가 존재하지 않아 날짜를 가져올 수 없습니다."))
                .getCreatedAt()
                .toLocalDate();
    }

    public String getMessages() {
        String messageSeparator = Long.toHexString(Double.doubleToLongBits(Math.random()));

        return letters.stream()
                .map(letter -> String.format(MERGE_FORMAT,
                        LETTER_SEPARATOR, messageSeparator,
                        replaceEscapeCharacters(letter.getMessage()),
                        LETTER_SEPARATOR, messageSeparator))
                .collect(Collectors.joining("\n"));
    }

    private String replaceEscapeCharacters(String message) {
        return message
                .replace(ESCAPE_LT, "&lt;")
                .replace(ESCAPE_GT, "&gt;")
                .replace(ESCAPE_AMP, "&amp;")
                .replace(ESCAPE_QUOT, "&quot;")
                .replace(ESCAPE_APOS, "&apos;");
    }
}
