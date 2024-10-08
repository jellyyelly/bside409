package bsise.server.auth.jwt;

public interface JwtConstant {

    String X_REFRESH_TOKEN = "X-Refresh-Token";
    String ISSUER = "upup-radio";
    int ACCESS_VALID_MILLIS = 1_000 * 60 * 30; // ms * s * m * h
    int REFRESH_VALID_MILLIS = 1_000 * 60 * 60; // ms * s * m * h
}
