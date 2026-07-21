package com.swiggysystem.orderservice.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // sirf checkout endpoint par hi rate limit lagao
        if (request.getRequestURI().equals("/api/orders/checkout")) {
            String clientKey = request.getRemoteAddr();   // real system mein userId (JWT se) use karenge, IP nahi
            Bucket bucket = rateLimiterService.resolveBucket(clientKey);

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);   // Too Many Requests
                response.getWriter().write("{\"error\":\"Rate limit exceeded, try again later\"}");
                response.setContentType("application/json");
                return;   // filter chain yahin रोको, controller tak request nahi jayegi
            }
        }

        filterChain.doFilter(request, response);
    }
}