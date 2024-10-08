package bsise.server.auth.jwt;

import static bsise.server.auth.jwt.JwtConstant.X_REFRESH_TOKEN;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtGeneratorFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("=== jwt generator filter start ===");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // authentication 으로부터 프로필 이미지 포함한 클레임 생성
            Claims claims = jwtService.makeNewClaims(authentication);

            // access token, refresh token 발행
            String accessToken = jwtService.issueAccessToken(claims);
            String refreshToken = jwtService.issueRefreshToken(claims);

            response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
            response.setHeader(X_REFRESH_TOKEN, "Bearer " + refreshToken);
        }

        log.info("=== jwt generator filter end ===");
        filterChain.doFilter(request, response);
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
        return Stream.of(
                "/error", "/swagger-", "/v3/api-docs", "/api-docs"
        ).noneMatch(uri -> request.getServletPath().startsWith(uri));
    }
}
