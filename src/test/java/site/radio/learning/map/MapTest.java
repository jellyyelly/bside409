package site.radio.learning.map;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import site.radio.reply.domain.Letter;
import site.radio.report.daily.domain.DailyReport;
import site.radio.user.domain.User;

public class MapTest {

    @DisplayName("Map 에서 value를 removeIf 할 시 key ,value 가 사라지는 지 검증 테스트")
    @Test
    void test() {
        // given
        Letter letter1 = createLetter(null);
        letter1.setDailyReport(DailyReport.builder().build());

        Letter letter2 = createLetter(null);
        letter2.setDailyReport(DailyReport.builder().build());

        Letter letter3 = createLetter(null);

        Map<LocalDate, List<Letter>> lettersByDate = new HashMap<>();
        lettersByDate.put(LocalDate.of(2024, 4, 11), Arrays.asList(letter1));
        lettersByDate.put(LocalDate.of(2024, 4, 12), Arrays.asList(letter2));
        lettersByDate.put(LocalDate.of(2024, 4, 13), Arrays.asList(letter3));

        System.out.println("기존 k, v");
        lettersByDate.forEach((k, v) -> System.out.println("k = " + k + " v = " + v));

        lettersByDate.values()
                .removeIf(letters -> letters.stream().anyMatch(letter -> letter.getDailyReport() != null));
        System.out.println("변경 k, v");
        lettersByDate.forEach((k, v) -> System.out.println("k = " + k + " v = " + v));

        System.out.println("Map 사이즈 = " + lettersByDate.size());
    }

    private Letter createLetter(User user) {
        return Letter.builder()
                .user(user)
                .build();
    }
}
