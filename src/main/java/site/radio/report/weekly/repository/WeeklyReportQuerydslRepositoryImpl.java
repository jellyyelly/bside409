package site.radio.report.weekly.repository;

import static site.radio.report.daily.domain.QDailyReport.dailyReport;
import static site.radio.report.daily.domain.QLetterAnalysis.letterAnalysis;
import static site.radio.report.weekly.domain.QWeeklyReport.weeklyReport;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import site.radio.error.WeeklyReportNotFoundException;
import site.radio.report.weekly.dto.WeeklyReportProjection;

public class WeeklyReportQuerydslRepositoryImpl implements WeeklyReportQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public WeeklyReportQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public WeeklyReportProjection findWeeklyReportDtoBy(UUID userId, LocalDate startDate, LocalDate endDate) {
        return queryFactory.from(letterAnalysis)
                .join(letterAnalysis.dailyReport, dailyReport)
                .join(dailyReport.weeklyReport, weeklyReport)
                .where(letterAnalysis.letter.user.id.eq(userId),
                        weeklyReport.startDate.eq(startDate),
                        weeklyReport.endDate.eq(endDate))
                .transform(GroupBy.groupBy(weeklyReport.id).as(
                        Projections.constructor(WeeklyReportProjection.class,
                                weeklyReport.weekOfYear,
                                weeklyReport.cheerUp,
                                weeklyReport.publishedCount,
                                weeklyReport.unpublishedCount,
                                weeklyReport.startDate,
                                weeklyReport.endDate,
                                GroupBy.list(dailyReport.coreEmotion)
                        )
                )).values().stream()
                .findAny()
                .orElseThrow(() -> new WeeklyReportNotFoundException("위클리 리포트를 찾지 못했습니다."));
    }
}
