package site.radio.report.daily.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import site.radio.auth.OAuth2Provider;
import site.radio.reply.domain.Letter;
import site.radio.reply.repository.LetterRepository;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;
import site.radio.report.weekly.dto.WeeklyLetterAnalyses;
import site.radio.user.domain.Preference;
import site.radio.user.domain.Role;
import site.radio.user.domain.User;
import site.radio.user.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
class DailyReportRepositoryTest {

    @Autowired
    private DailyReportRepository dailyReportRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Autowired
    private LetterAnalysisRepository letterAnalysisRepository;

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
        User user = createUser("사용자이름1", "이메일1", "닉네임1");

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

        Letter letter1 = createPublishedLetter(user);
        LetterAnalysis letterAnalysis1 = LetterAnalysis.builder()
                .letter(letter1)
                .dailyReport(dailyReport1)
                .build();
        Letter letter2 = createPublishedLetter(user);
        LetterAnalysis letterAnalysis2 = LetterAnalysis.builder()
                .letter(letter2)
                .dailyReport(dailyReport2)
                .build();
        Letter letter3 = createPublishedLetter(user);
        LetterAnalysis letterAnalysis3 = LetterAnalysis.builder()
                .letter(letter3)
                .dailyReport(dailyReport3)
                .build();

        letterAnalysisRepository.saveAll(List.of(letterAnalysis1, letterAnalysis2, letterAnalysis3));

        LocalDate startDate = LocalDate.of(2024, 11, 11);

        // when
        List<LetterAnalysis> analyses = letterAnalysisRepository.findLetterAnalysesByDateRangeIn(user.getId(),
                startDate, startDate.plusDays(6));
        WeeklyLetterAnalyses weeklyLetterAnalyses = WeeklyLetterAnalyses.of(analyses, startDate, startDate.plusDays(6));

        // then
        assertThat(weeklyLetterAnalyses.getDailyReports()).hasSize(2);
        assertThat(weeklyLetterAnalyses.getDailyReports().stream().map(DailyReport::getTargetDate).toList())
                .containsAnyOf(LocalDate.of(2024, 11, 13), LocalDate.of(2024, 11, 15)
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
        LetterAnalysis letterAnalysis1 = LetterAnalysis.builder()
                .letter(letter1)
                .dailyReport(dailyReport)
                .build();
        Letter letter2 = createPublishedLetter(user);
        LetterAnalysis letterAnalysis2 = LetterAnalysis.builder()
                .letter(letter2)
                .dailyReport(dailyReport)
                .build();
        Letter letter3 = createPublishedLetter(user);
        LetterAnalysis letterAnalysis3 = LetterAnalysis.builder()
                .letter(letter3)
                .dailyReport(dailyReport)
                .build();

        Letter letter4 = createPublishedLetter(user);

        Letter letter5 = createUnPublishedLetter(user);
        LetterAnalysis letterAnalysis5 = LetterAnalysis.builder()
                .letter(letter5)
                .dailyReport(dailyReport)
                .build();

        letterRepository.saveAll(List.of(letter1, letter2, letter3, letter4, letter5));
        letterAnalysisRepository.save(letterAnalysis5);
        letterAnalysisRepository.saveAll(List.of(letterAnalysis1, letterAnalysis2, letterAnalysis3));

        // when
        List<LetterAnalysis> analyses = letterAnalysisRepository.findLetterAnalysesByDateRangeIn(user.getId(), start,
                start.plusDays(6));
        WeeklyLetterAnalyses weeklyLetterAnalyses = WeeklyLetterAnalyses.of(analyses, start, start.plusDays(6));

        // then
        assertThat(weeklyLetterAnalyses.getPublishedCount()).isEqualTo(3);
        assertThat(weeklyLetterAnalyses.getUnpublishedCount()).isEqualTo(1);
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