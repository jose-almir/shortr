package com.almirdev.shortr.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
public class IPKeyResolver implements RateLimitKeyResolver {
    private final HttpServletRequest request;

    public IPKeyResolver(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public String resolve(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String clientIp = resolveClientIp();
        String endpoint = signature.getDeclaringType().getSimpleName() + "#" + signature.getMethod().getName();
        return "rate-limit:" + clientIp + ":" + request.getMethod() + ":" + endpoint;
    }

    private String resolveClientIp() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

