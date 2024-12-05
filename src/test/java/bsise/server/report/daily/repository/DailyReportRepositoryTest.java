package bsise.server.report.daily.repository;

import static org.assertj.core.api.Assertions.assertThat;

import bsise.server.auth.OAuth2Provider;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.report.daily.domain.CoreEmotion;
import bsise.server.report.daily.domain.DailyReport;
import bsise.server.report.weekly.dto.WeeklyPublishedStaticsDto;
import bsise.server.user.domain.Preference;
import bsise.server.user.domain.Role;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DailyReportRepositoryTest {

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LetterRepository letterRepository;

    @AfterEach
    void tearDown() {
        letterRepository.deleteAllInBatch();
        dailyReportRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("시작날짜로부터 1주일간 생성된 일일 통계들을 찾는다")
    @Test
    void findCreatedDailyReportsWithinOneWeekByStartDate() {
        // given
        DailyReport dailyReport1 = DailyReport.builder()
                .coreEmotion(CoreEmotion.기쁨)
                .targetDate(LocalDate.of(2024, 11, 13))
                .description("해석1")
                .build();
        DailyReport dailyReport2 = DailyReport.builder()
                .coreEmotion(CoreEmotion.분노)
                .targetDate(LocalDate.of(2024, 11, 15))
                .description("해석2")
                .build();
        DailyReport dailyReport3 = DailyReport.builder()
                .coreEmotion(CoreEmotion.놀라움)
                .targetDate(LocalDate.of(2024, 11, 18))
                .description("해석3")
                .build();
        dailyReportRepository.saveAll(List.of(dailyReport1, dailyReport2, dailyReport3));

        LocalDate startDate = LocalDate.of(2024, 11, 11);

        // when
        List<LocalDate> oneWeekDates = IntStream.range(0, 7)
                .mapToObj(startDate::plusDays)
                .collect(Collectors.toList());

        List<DailyReport> reports = dailyReportRepository.findByTargetDateIn(oneWeekDates);

        // then
        assertThat(reports).hasSize(2);
        assertThat(reports).extracting("targetDate").containsAnyOf(
                LocalDate.of(2024, 11, 13),
                LocalDate.of(2024, 11, 15)
        );
    }

    @DisplayName("일주일 단위로 일일분석에 사용된 편지의 총 개수를 구한다.")
    @Test
    void findPublishedLettersCountOfDailyReportByOneWeek() {
        // given
        LocalDate start = LocalDate.of(2024, 11, 15);
        User user = createUser("사용자이름1", "이메일1", "닉네임1");
        userRepository.save(user);

        DailyReport dailyReport = DailyReport.builder()
                .coreEmotion(CoreEmotion.기쁨)
                .targetDate(LocalDate.of(2024, 11, 16))
                .description("해석1")
                .build();
        dailyReportRepository.save(dailyReport);

        Letter letter1 = createPublishedLetter(user);
        letter1.setDailyReport(dailyReport);
        Letter letter2 = createPublishedLetter(user);
        letter2.setDailyReport(dailyReport);
        Letter letter3 = createPublishedLetter(user);
        letter3.setDailyReport(dailyReport);

        Letter letter4 = createPublishedLetter(user);

        Letter letter5 = createUnPublishedLetter(user);
        letter5.setDailyReport(dailyReport);
        letterRepository.saveAll(List.of(letter1, letter2, letter3, letter4, letter5));

        // when
        WeeklyPublishedStaticsDto staticsDto = dailyReportRepository.findPublishedStatics(
                IntStream.rangeClosed(0, 6)
                        .mapToObj(start::plusDays)
                        .toList());

        // then
        assertThat(staticsDto.getPublishedCount()).isEqualTo(3);
        assertThat(staticsDto.getUnPublishedCount()).isEqualTo(1);
    }

    private Letter createPublishedLetter(User user) {
        return Letter.builder()
                .user(user)
                .published(true)
                .build();
    }

    private Letter createUnPublishedLetter(User user) {
        return Letter.builder()
                .user(user)
                .published(false)
                .build();
    }

    private User createUser(String username, String email, String nickname) {
        return User.builder()
                .username(username)
                .email(email)
                .nickname(nickname)
                .preference(Preference.T)
                .provider(OAuth2Provider.UNKNOWN)
                .role(Role.GUEST)
                .build();
    }
}