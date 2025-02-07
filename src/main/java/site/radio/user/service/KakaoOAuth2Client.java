package site.radio.user.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import site.radio.user.dto.KakaoUnlinkResponse;

@FeignClient(name = "kakao-oauth2-service", url = "https://kapi.kakao.com/v1/user/unlink")
public interface KakaoOAuth2Client {

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    KakaoUnlinkResponse requestUnlinkByUser(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String appAdminKey,
            @RequestParam(name = "target_id_type", defaultValue = "user_id") String targetIdType,
            @RequestParam(name = "target_id") String targetId
    );
}
