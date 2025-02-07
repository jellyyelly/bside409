package site.radio.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.radio.common.BaseTimeEntity;
import site.radio.user.dto.UserDeleteRequest;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "withdrawl_user")
public class WithdrawalUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawl_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "reason")
    private WithdrawalReason reason;

    @Column(name = "detail_reason")
    private String detailReason;

    @Builder
    public WithdrawalUser(User user, WithdrawalReason reason, String detailReason) {
        this.user = user;
        this.reason = reason;
        this.detailReason = detailReason;
    }

    public static WithdrawalUser toWithdrawalUser(User user, UserDeleteRequest deleteRequestDto) {
        return WithdrawalUser.builder()
                .user(user)
                .reason(deleteRequestDto.getWithdrawalReason())
                .detailReason(deleteRequestDto.getDetailReason().isPresent() ? deleteRequestDto.getDetailReason().get()
                        : null)
                .build();
    }
}
