package site.radio.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import site.radio.auth.KakaoUserInfo;
import site.radio.user.domain.User;

@Getter
@RequiredArgsConstructor
public class KakaoUnlinkRequest {

    public static final int KAKAO_ID_POSITION = 0;

    @JsonProperty(value = "target_id_type")
    private String targetIdType = "user_id";

    @JsonProperty(value = "target_id")
    private final String targetId;

    public static KakaoUnlinkRequest of(User user) {
        return new KakaoUnlinkRequest(extractKakaoIdByUsername(user.getUsername()));
    }

    public static String extractKakaoIdByUsername(String username) {
        return username.split(KakaoUserInfo.DELIMITER)[KAKAO_ID_POSITION];
    }
}
