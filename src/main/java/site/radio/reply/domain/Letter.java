package site.radio.reply.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.radio.common.BaseTimeEntity;
import site.radio.report.daily.domain.DailyReport;
import site.radio.user.domain.Preference;
import site.radio.user.domain.User;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "letter")
public class Letter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "letter_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_report_id")
    private DailyReport dailyReport;

    @Column(name = "message")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference")
    private Preference preference;

    @Column(name = "published")
    private boolean published;

    @Column(name = "like_t")
    private Long likeT;

    @Column(name = "like_f")
    private Long likeF;

    @Builder
    public Letter(User user, String message, Preference preference, boolean published) {
        this.user = user;
        this.message = message;
        this.preference = preference;
        this.published = published;
        this.likeT = 0L;
        this.likeF = 0L;
    }

    public void plusLikeT() {
        likeT++;
    }

    public void plusLikeF() {
        likeF++;
    }

    public void cancelLikeT() {
        likeT--;
    }

    public void cancelLikeF() {
        likeF--;
    }
}
