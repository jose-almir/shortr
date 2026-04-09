package com.almirdev.shortr.infrastructure.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
public class RateLimitAspect {
    private final RateLimiterService rateLimiterService;
    private final RateLimitKeyResolver rateLimitKeyResolver;

    public RateLimitAspect(RateLimiterService rateLimiterService, RateLimitKeyResolver resolver) {
        this.rateLimiterService = rateLimiterService;
        this.rateLimitKeyResolver = resolver;
    }

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimitKeyResolver.resolve(joinPoint);
        BucketConfiguration configuration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(rateLimit.capacity())
                        .refillGreedy(rateLimit.refill(), Duration.of(rateLimit.period(), rateLimit.unit())))
                .build();

        Bucket bucket = rateLimiterService.resolveBucket(key, () -> configuration);

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        } else {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletResponse response = attributes.getResponse();
                if (response != null) {
                    response.setStatus(429);
                }
            }

            throw new RateLimitException("Too many requests. Please try again later.");
        }
    }
}
