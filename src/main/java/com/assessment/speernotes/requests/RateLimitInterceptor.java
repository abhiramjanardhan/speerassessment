package com.assessment.speernotes.requests;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {
    private static final long TIME_WINDOW_MS = 60 * 1000; // 1 minute window
    private static final int MAX_REQUESTS = 5; // Max 5 requests per minute

    private final Map<String, UserRequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIp = request.getRemoteAddr(); // Use user IP (or username if authenticated)
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
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.getWriter().write("Too many requests. Try again later.");
                return false; // Block the request
            }
        }
        return true; // Allow the request
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
