package com.mipt.portal.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();
    private final long windowMs;
    private final int limit;

    public RateLimitInterceptor(
            @Value("${security.rate-limit.window-ms:60000}") long windowMs,
            @Value("${security.rate-limit.limit:30}") int limit) {
        this.windowMs = windowMs;
        this.limit = limit;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = resolveKey();
        long now = Instant.now().toEpochMilli();
        Deque<Long> deque = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!deque.isEmpty() && now - deque.peekFirst() > windowMs) {
            deque.pollFirst();
        }

        if (deque.size() >= limit) {
            response.setStatus(429);
            return false;
        }

        deque.addLast(now);
        return true;
    }

    private String resolveKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "anon";
        }
        return "user:" + authentication.getName();
    }
}
