package com.recrutech.recrutechauth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Filter for rate limiting requests to authentication endpoints.
 * This helps prevent brute force attacks.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    
    @Value("${app.rate-limiting.enabled:true}")
    private boolean enabled;
    
    @Value("${app.rate-limiting.limit:10}")
    private int limit;
    
    @Value("${app.rate-limiting.refresh-period:60}")
    private int refreshPeriod;
    
    @Value("${app.rate-limiting.timeout-duration:30}")
    private int timeoutDuration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Only apply rate limiting to authentication endpoints
        if (!enabled || !isAuthEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = getClientIp(request);
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());
        
        if (counter.isBlocked()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        
        if (counter.incrementAndGet() > limit) {
            counter.block();
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Checks if the request URI is for an authentication endpoint.
     *
     * @param uri the request URI
     * @return true if the URI is for an authentication endpoint, false otherwise
     */
    private boolean isAuthEndpoint(String uri) {
        return uri.contains("/auth/login") || uri.contains("/auth/register") || uri.contains("/auth/refresh");
    }
    
    /**
     * Gets the client IP address from the request.
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    /**
     * Counter for tracking request counts and blocking status.
     */
    private class RequestCounter {
        private int count;
        private long lastResetTime;
        private long blockedUntil;
        
        public RequestCounter() {
            this.count = 0;
            this.lastResetTime = System.currentTimeMillis();
            this.blockedUntil = 0;
        }
        
        /**
         * Increments the request count and resets it if the refresh period has elapsed.
         *
         * @return the new count
         */
        public synchronized int incrementAndGet() {
            long now = System.currentTimeMillis();
            if (now - lastResetTime > TimeUnit.SECONDS.toMillis(refreshPeriod)) {
                count = 0;
                lastResetTime = now;
            }
            return ++count;
        }
        
        /**
         * Blocks the client for the configured timeout duration.
         */
        public synchronized void block() {
            blockedUntil = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutDuration);
        }
        
        /**
         * Checks if the client is currently blocked.
         *
         * @return true if the client is blocked, false otherwise
         */
        public synchronized boolean isBlocked() {
            return System.currentTimeMillis() < blockedUntil;
        }
    }
}