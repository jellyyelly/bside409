package site.radio.report.weekly.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.error.LetterAnalysisNotFoundException;
import site.radio.reply.domain.Letter;
import site.radio.report.daily.domain.CoreEmotion;
import site.radio.report.daily.domain.DailyReport;
import site.radio.report.daily.domain.LetterAnalysis;

/**
 * 1주일치 {@code List<LetterAnalysis>}를 의미하는 래퍼 클래스(wrapper class)입니다. {@link Letter}, {@link DailyReport} 관련 정보를 그래프 탐색하여
 * 필요한 정보를 전달하는데 목적을 둡니다.
 */
@RequiredArgsConstructor
public class WeeklyLetterAnalyses {

    private final List<LetterAnalysis> letterAnalyses;

    @Getter
    private final LocalDate startDate;

    @Getter
    private final LocalDate endDate;

    public static WeeklyLetterAnalyses of(List<LetterAnalysis> letterAnalyses, LocalDate startDate, LocalDate endDate) {
        if (letterAnalyses.isEmpty()) {
            throw new LetterAnalysisNotFoundException("분석된 편지가 없습니다.");
        }
        return new WeeklyLetterAnalyses(letterAnalyses, startDate, endDate);
    }

    public String getMergedDescription() {
        return letterAnalyses.stream()
                .map(LetterAnalysis::getDailyReport)
                .map(DailyReport::getDescription)
                .collect(Collectors.joining());
    }

    public int getPublishedCount() {
        return (int) letterAnalyses.stream()
                .map(LetterAnalysis::getLetter)
                .filter(Letter::isPublished)
                .count();
    }

    public int getUnpublishedCount() {
        return (int) letterAnalyses.stream()
                .map(LetterAnalysis::getLetter)
                .filter(letter -> !letter.isPublished())
                .count();
    }

    public List<DailyReport> getDailyReports() {
        return letterAnalyses.stream()
                .map(LetterAnalysis::getDailyReport)
                .toList();
    }

    public List<CoreEmotion> getCoreEmotions() {
        return letterAnalyses.stream()
                .map(LetterAnalysis::getDailyReport)
                .map(DailyReport::getCoreEmotion)
                .toList();
    }
}
