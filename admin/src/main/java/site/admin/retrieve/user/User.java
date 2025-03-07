package site.admin.retrieve.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "nickname", nullable = false, length = 12)
    private String nickname;

    @Column(name = "preference", nullable = false)
    private String preference;

    @Column(name = "is_synced")
    private boolean isSynced = false;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_email_ads_consented")
    private boolean isEmailAdsConsented = true;

    @Column(name = "isDormant") // TODO: column 이름 변경
    private boolean isDormant = false;

    @Column(name = "dormant_at")
    private LocalDateTime dormantAt;

    @Column(name = "agree_to_terms")
    private boolean agreeToTerms = false;

    @Column(name = "agree_to_privacy_policy")
    private boolean agreeToPrivacyPolicy = false;

    @Column(name = "oauth2_provider", nullable = false)
    private String provider;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
