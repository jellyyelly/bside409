package site.radio.auth.jwt;

public interface JwtConstant {

    String X_REFRESH_TOKEN = "X-Refresh-Token";
    String ISSUER = "upup-radio";
    int ACCESS_VALID_MILLIS = 1_000 * 60 * 30; // ms * s * m
    int REFRESH_VALID_MILLIS = 1_000 * 60 * 60 * 24 * 7; // ms * s * m * h * d
}
