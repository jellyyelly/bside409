package site.radio.report.weekly.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.common.BaseTimeEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weekly_report")
public class WeeklyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "weekly_report_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "week_of_year", nullable = false)
    private int weekOfYear;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "published_count", nullable = false)
    private int publishedCount;

    @Column(name = "unpublished_count", nullable = false)
    private int unpublishedCount;

    @Column(name = "cheer_up", nullable = false, length = 1000)
    private String cheerUp;

    @Builder
    public WeeklyReport(int weekOfYear, LocalDate startDate, LocalDate endDate, int publishedCount,
                        int unpublishedCount, String cheerUp) {
        this.weekOfYear = weekOfYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.publishedCount = publishedCount;
        this.unpublishedCount = unpublishedCount;
        this.cheerUp = cheerUp;
    }
}
