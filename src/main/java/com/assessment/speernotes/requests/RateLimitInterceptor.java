package com.assessment.speernotes.requests;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1-minute window
    public static final int MAX_REQUESTS = 5; // Max 5 requests per minute
    private static final int THROTTLE_DELAY_MS = 500; // 500ms delay for excessive requests

    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Value("${rate-limiting.enabled:true}")  // Default to true if the property is not found
    boolean rateLimitingEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!rateLimitingEnabled) {
            return true;  // Skip rate limiting logic if disabled
        }

        String userIp = request.getRemoteAddr(); // Identify users (IP-based, can use auth)
        Instant now = Instant.now();

        requestCounts.putIfAbsent(userIp, new UserRequestInfo());

        UserRequestInfo userInfo = requestCounts.get(userIp);

        synchronized (userInfo) {
            // Reset counter if time window expired
            if (now.toEpochMilli() - userInfo.startTime.toEpochMilli() > TIME_WINDOW_MS) {
                userInfo.startTime = now;
                userInfo.requestCount = 0;
            }

            // Increment request count
            userInfo.requestCount++;

            if (userInfo.requestCount > MAX_REQUESTS) {
                // Apply throttling instead of blocking
                Thread.sleep(THROTTLE_DELAY_MS);
                response.addHeader("X-RateLimit-Throttled", "true");
            }

            if (userInfo.requestCount > MAX_REQUESTS * 2) {
                // Hard block if excessive abuse
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.getWriter().write("Too many requests. Try again later.");
                return false;
            }
        }

        return true; // Allow request
    }

    private static class UserRequestInfo {
        private Instant startTime;
        private int requestCount;

        UserRequestInfo() {
            this.startTime = Instant.now();
            this.requestCount = 0;
        }
    }
}
