package site.radio.auth.jwt;

import static site.radio.auth.jwt.JwtConstant.X_REFRESH_TOKEN;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Profile({"prod"})
@RequiredArgsConstructor
public class JwtValidatorFilter extends OncePerRequestFilter {

    private static final String[] NOT_FILTERED_URLS = {
            "/login*", "/oauth2*", "/error", "/swagger-*", "/v3/api-docs*", "/api-docs*", "/api/v1/users*",
            "/actuator/**"
    };
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== jwt validator filter start ===");
        String accessToken = jwtService.resolveAccessToken(request);
        String refreshToken = jwtService.resolveRefreshToken(request);

        log.info("=== accessToken: {}", accessToken);
        log.info("=== refreshToken: {}", refreshToken);

        // validate
        try {
            if (accessToken == null || !jwtService.isValidAccessToken(accessToken)) {
                throw new BadCredentialsException("Invalid access token");
            }
            // security context 저장
            Authentication authentication = jwtService.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 기존 accessToken, refreshToken 반환
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            response.setHeader(X_REFRESH_TOKEN, "Bearer " + refreshToken);
            log.info("=== JWT IN HEADER: 최초 ===");
        } catch (ExpiredJwtException e) {
            if (refreshToken == null || !jwtService.isValidRefreshToken(refreshToken)) {
                throw new BadCredentialsException("Invalid refresh token");
            }
            // 기존 accessToken 기반 새로운 accessToken 생성
            accessToken = jwtService.reIssueAccessToken(refreshToken);
            refreshToken = jwtService.reIssueRefreshToken(refreshToken);

            // security context 저장
            Authentication authentication = jwtService.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 갱신된 accessToken, refreshToken 반환
            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            response.setHeader(X_REFRESH_TOKEN, "Bearer " + refreshToken);
            log.info("=== JWT Refresh ===");
        }

        filterChain.doFilter(request, response);
        log.info("===jwt validator filter end===");
    }

    /**
     * servlet path 가 허용된 로그인 URL 이 아닐 때는 해당 필터를 작동시키지 않도록 합니다.
     *
     * @param request HttpServletRequest
     * @return true: 해당 필터 적용하지 않음, false: 해당 필터를 적용함
     * @throws ServletException 서블릿 예외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return PatternMatchUtils.simpleMatch(NOT_FILTERED_URLS, request.getServletPath());
    }
}
