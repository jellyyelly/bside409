package bsise.server.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static bsise.server.auth.jwt.JwtConstant.EXPIRED_ACCESS_TOKEN;
import static bsise.server.auth.jwt.JwtConstant.X_REFRESH_TOKEN;

@Slf4j
@Profile({"prod"})
@RequiredArgsConstructor
public class JwtRefreshFilter extends OncePerRequestFilter {

    private static final String[] NOT_FILTERED_URLS = {
            "/login", "/oauth2*", "/error", "/swagger-*", "/v3/api-docs*", "/api-docs*", "/api/v1/users*"
    };
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== jwt refresh filter start ===");
        String expiredAccessToken = (String) request.getAttribute(EXPIRED_ACCESS_TOKEN);
        String refreshToken = jwtService.resolveRefreshToken(request);

        log.info("=== refreshToken: {}", refreshToken);

        if (expiredAccessToken != null) {
            if (refreshToken == null) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            try {
                if (jwtService.isValidRefreshToken(refreshToken)) {
                    // security context 저장
                    Authentication authentication = jwtService.getAuthentication(refreshToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 기존 accessToken 기반 새로운 accessToken 생성
                    String reIssuedAccessToken = jwtService.reIssueAccessToken(refreshToken);
                    String reIssuedRefreshToken = jwtService.reIssueRefreshToken(refreshToken);

                    // 갱신된 accessToken, refreshToken 반환
                    response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + reIssuedAccessToken);
                    response.setHeader(X_REFRESH_TOKEN, "Bearer " + reIssuedRefreshToken);
                    log.info("=== JWT token refreshed ===");
                }
            } catch (ExpiredJwtException e) {
                log.info("=== JWT refresh token expired ===");
                throw new BadCredentialsException("Invalid refresh token");
            }
        }

        filterChain.doFilter(request, response);
        log.info("===jwt refresh filter end===");
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
