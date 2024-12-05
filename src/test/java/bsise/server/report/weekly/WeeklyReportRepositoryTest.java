package bsise.server.report.weekly;

import bsise.server.auth.OAuth2Provider;
import bsise.server.letter.Letter;
import bsise.server.letter.LetterRepository;
import bsise.server.report.daily.domain.CoreEmotion;
import bsise.server.report.daily.domain.DailyReport;
import bsise.server.report.daily.repository.DailyReportRepository;
import bsise.server.report.weekly.domain.WeeklyReport;
import bsise.server.report.weekly.repository.WeeklyReportRepository;
import bsise.server.user.domain.Preference;
import bsise.server.user.domain.Role;
import bsise.server.user.domain.User;
import bsise.server.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Profile;

@DataJpaTest
@Profile("test")
class WeeklyReportRepositoryTest {

    @Autowired
    private WeeklyReportRepository weeklyReportRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이미 주간 분석된 것을 찾을 수 있다.")
    @Test
    void test() {
        // given
        LocalDate start = LocalDate.of(2024, 11, 20);
        LocalDate end = LocalDate.of(2024, 11, 26);
        WeeklyReport weeklyReport = WeeklyReport.builder()
                .startDate(start)
                .endDate(end)
                .weekOfYear(35)
                .publishedCount(5)
                .unpublishedCount(5)
                .cheerUp("위로한마디")
                .build();
        weeklyReportRepository.save(weeklyReport);

        User user1 = createUser("사용자이름1", "이메일1", "닉네임1");
        User user2 = createUser("사용자이름2", "이메일2", "닉네임2");
        User user3 = createUser("사용자이름3", "이메일3", "닉네임3");
        userRepository.saveAll(List.of(user2, user3, user1));

        Letter letter1 = createLetter(user1);
        Letter letter2 = createLetter(user2);
        Letter letter3 = createLetter(user3);

        letterRepository.saveAll(List.of(letter1, letter2, letter3));

        DailyReport dailyReport = DailyReport.builder()
                .targetDate(LocalDate.of(2024, 11, 25))
                .coreEmotion(CoreEmotion.기쁨)
                .description("설명1")
                .build();
        letter1.setDailyReport(dailyReport);
        dailyReport.setWeeklyReport(weeklyReport);
        dailyReportRepository.save(dailyReport);

        // when
        Optional<WeeklyReport> optWeeklyReport = weeklyReportRepository.findDailyReportBy(
                user1.getId(), start, end);

        // then
        Assertions.assertThat(optWeeklyReport).isNotEmpty();
    }

    private Letter createLetter(User user) {
        return Letter.builder()
                .user(user)
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