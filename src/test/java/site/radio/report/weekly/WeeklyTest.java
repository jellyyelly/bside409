package site.radio.report.weekly;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


public class WeeklyTest {

    @DisplayName("사용가능한 language 와 country 목록을 출력한다")
    @Test
    void test1() {
        // given
        Locale[] locales = Locale.getAvailableLocales();

        // when then
        Arrays.stream(locales)
                .forEach(locale -> System.out.println(
                        "country: " + locale.getCountry() +
                        " / " +
                        "language: " + locale.getLanguage()
                ));
    }

    @DisplayName("2024년 12월 30일은 ISO 기준으로 1월 1주차이다.")
    @Test
    void ISO_기준으로_특정날짜에대해_몇월_몇주차를_구한다() {
        // given
        LocalDate target = LocalDate.of(2024, 12, 30);
        LocalDate startOfWeek = target.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)); // 해당 주의 월요일
        LocalDate endOfWeek = target.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));    // 해당 주의 일요일
        LocalDate middleOfWeek = startOfWeek.plusDays(3);  // 해당 주의 중간일

        // when
        int monthValue = middleOfWeek.getMonthValue();
        int weekValue = middleOfWeek.get(WeekFields.ISO.weekOfMonth());

        //then
        Assertions.assertThat(startOfWeek).isEqualTo(LocalDate.of(2024, 12, 30));
        Assertions.assertThat(endOfWeek).isEqualTo(LocalDate.of(2025, 1, 5));
        Assertions.assertThat(monthValue).isEqualTo(1);
        Assertions.assertThat(weekValue).isEqualTo(1);

        System.out.println("해당 날짜가 포함된 주의 월요일: " + startOfWeek);  // 12월 30일
        System.out.println("해당 날짜가 포함된 주의 일요일: " + endOfWeek);    // 1월 5일
        System.out.println("해당 날짜의 몇 월: " + monthValue);        // 1월
        System.out.println("해당 날짜의 몇 주차: " + weekValue);    // 1주차

        System.out.println(target.get(WeekFields.ISO.weekOfWeekBasedYear())); // ISO 기준
        System.out.println(target.get(WeekFields.ISO.weekOfYear()));          // ISO 기준 X
    }

    @DisplayName("24년 11월 16일 기준으로 4주차의 날짜를 구한다.")
    @Test
    void test10() {
        // given
        LocalDate today = LocalDate.of(2025, 1, 16);

        List<WeeklyDate> weeklyDates = IntStream.rangeClosed(1, 4)
                .mapToObj(i -> {
                    LocalDate minusDate = today.minusDays((long) i * 7);
                    LocalDate firstOfWeek = minusDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    LocalDate lastOfWeek = minusDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                    LocalDate middleOfWeek = firstOfWeek.plusDays(3);

                    return new WeeklyDate(
                            firstOfWeek,
                            lastOfWeek,
                            middleOfWeek.getMonthValue(),
                            middleOfWeek.get(WeekFields.ISO.weekOfMonth())
                    );
                })
                .sorted()
                .collect(Collectors.toList());

        for (WeeklyDate weeklyDate : weeklyDates) {
            System.out.println("weeklyDate = " + weeklyDate);
        }
    }

    static class WeeklyDate implements Comparable<WeeklyDate> {
        LocalDate startDate;
        LocalDate endDate;
        int month;
        int weekNumber;

        public WeeklyDate(LocalDate startDate, LocalDate endDate, int month, int weekNumber) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.month = month;
            this.weekNumber = weekNumber;
        }

        @Override
        public int compareTo(@NotNull WeeklyTest.WeeklyDate o) {
            return startDate.compareTo(o.startDate);
        }

        @Override
        public String toString() {
            return "WeeklyDate{" +
                   "startDate=" + startDate +
                   ", endDate=" + endDate +
                   ", month=" + month +
                   ", weekNumber=" + weekNumber +
                   '}';
        }
    }
}
