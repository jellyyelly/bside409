package bsise.server.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.web.filter.OncePerRequestFilter;

public class CookieEncodingFilter extends OncePerRequestFilter {

    private final String[] cookiesToEncode;

    public CookieEncodingFilter(String... cookiesToEncode) {
        this.cookiesToEncode = cookiesToEncode;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                try {
                    String encodedValue = encodeCookieValue(cookie.getValue());
                    Cookie newCookie = new Cookie(cookie.getName(), encodedValue);
                    newCookie.setPath("/");
                    response.addCookie(newCookie);
                } catch (Exception e) {
                    logger.warn("Failed to encode cookie: " + cookie.getName(), e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String encodeCookieValue(String value) throws Exception {
        // First, URL encode the value
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.name());

        // If the value contains non-ASCII characters after URL encoding, use Base64 encoding
        if (!encoded.equals(value)) {
            byte[] encodedBytes = Base64.getEncoder().encode(value.getBytes(StandardCharsets.UTF_8));
            encoded = new String(encodedBytes, StandardCharsets.UTF_8);
        }

        return encoded;
    }
}
