package com.platform.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter extends OncePerRequestFilter {

    // Ограничение: максимум 10 попыток авторизации в минуту с одного IP адреса
    private static final int MAX_REQUESTS_PER_MINUTE = 10;
    private static final long ONE_MINUTE_MILLIS = 60_000L;

    private static class ClientRequestInfo {
        long windowStart;
        final AtomicInteger count;

        ClientRequestInfo(long windowStart) {
            this.windowStart = windowStart;
            this.count = new AtomicInteger(1);
        }
    }

    private final Map<String, ClientRequestInfo> ipRequests = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/v1/auth/")) {
            String clientIp = getClientIp(request);
            long currentTime = System.currentTimeMillis();

            ClientRequestInfo info = ipRequests.compute(clientIp, (ip, currentInfo) -> {
                if (currentInfo == null || (currentTime - currentInfo.windowStart) > ONE_MINUTE_MILLIS) {
                    return new ClientRequestInfo(currentTime);
                } else {
                    currentInfo.count.incrementAndGet();
                    return currentInfo;
                }
            });

            if (info.count.get() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // HTTP 429
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("""
                    {
                      "code": "RATE_LIMIT_EXCEEDED",
                      "message": "Превышен лимит запросов на авторизацию. Повторите попытку через минуту.",
                      "details": null
                    }
                    """);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(",")) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}