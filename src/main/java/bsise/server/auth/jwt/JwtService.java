package bsise.server.auth.jwt;

import bsise.server.auth.UpOAuth2UserService;
import bsise.server.auth.UpUserDetails;
import bsise.server.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static bsise.server.auth.jwt.JwtConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtService {

    @Value("${security.jwt.token.access-key}")
    private String accessKey;

    @Value("${security.jwt.token.refresh-key}")
    private String refreshKey;

    private final UpOAuth2UserService oAuth2UserService;
    private SecretKey accessSecretKey;
    private SecretKey refreshSecretKey;

    @PostConstruct
    protected void init() {
        accessSecretKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        refreshSecretKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
    }

    public String issueAccessToken(Claims claims) {
        return issueToken(claims, accessSecretKey, ACCESS_VALID_MILLIS)
                .compact();
    }

    public String issueRefreshToken(Claims claims) {
        return issueToken(claims, refreshSecretKey, REFRESH_VALID_MILLIS)
                .compact();
    }

    private JwtBuilder issueToken(Claims claims, SecretKey secretKey, int expireAt) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(JwtConstant.ISSUER)
                .claims(claims)
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiration(Timestamp.valueOf(LocalDateTime.now().plus(expireAt, ChronoUnit.MILLIS)))
                .signWith(secretKey, SIG.HS256);
    }

    public String reIssueAccessToken(String jwt) {
        return reIssue(getClaimsFromAccessToken(jwt), accessSecretKey, ACCESS_VALID_MILLIS);
    }

    public String reIssueRefreshToken(String jwt) {
        return reIssue(getClaimsFromRefreshToken(jwt), refreshSecretKey, REFRESH_VALID_MILLIS);
    }

    private String reIssue(Claims oldClaims, SecretKey secretKey, int expireAt) {
        return Jwts.builder()
                .id(oldClaims.getId())
                .issuer(oldClaims.getIssuer())
                .subject(oldClaims.getSubject())
                .issuedAt(Timestamp.valueOf(LocalDateTime.now()))
                .expiration(Timestamp.valueOf(LocalDateTime.now().plus(expireAt, ChronoUnit.MILLIS)))
                .signWith(secretKey, SIG.HS256)
                .compact();
    }

    public Authentication getAuthentication(String jwt) {
        String userId = getUserId(jwt);
        UserDetails userDetails = oAuth2UserService.loadUserByUsername(userId);

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserId(String jwt) {
        return Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload()
                .getSubject();
    }

    public Claims makeNewClaims(Authentication authentication) {
        UpUserDetails principal = (UpUserDetails) authentication.getPrincipal();
        User authenticatedUser = principal.getUser();
        String userId = authenticatedUser.getId().toString();
        String profileImageUrl = authenticatedUser.getProfileImageUrl();

        return Jwts.claims()
                .subject(userId)
                .add("role", authentication.getAuthorities())
                .add("profileImageUrl", profileImageUrl)
                .build();
    }

    public Claims getClaimsFromAccessToken(String jwt) {
        return Jwts.parser()
                .verifyWith(accessSecretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public Claims getClaimsFromRefreshToken(String jwt) {
        return Jwts.parser()
                .verifyWith(refreshSecretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return resolveToken(request, HttpHeaders.AUTHORIZATION);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveToken(request, X_REFRESH_TOKEN);
    }

    private String resolveToken(HttpServletRequest request, String header) {
        String bearerToken = request.getHeader(header);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean isValidAccessToken(String jwt) {
        return validateToken(jwt, accessSecretKey);
    }

    public boolean isValidRefreshToken(String jwt) {
        return validateToken(jwt, refreshSecretKey);
    }

    private boolean validateToken(String jwt, SecretKey secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt);
            return true;
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid JWT token.");
        }
    }
}
