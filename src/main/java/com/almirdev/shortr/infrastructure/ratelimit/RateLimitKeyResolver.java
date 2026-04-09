package com.almirdev.shortr.infrastructure.ratelimit;

import org.aspectj.lang.JoinPoint;

public interface RateLimitKeyResolver {
    String resolve(JoinPoint joinPoint);
}
