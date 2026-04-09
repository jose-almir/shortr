package com.almirdev.shortr.infrastructure.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    long capacity() default 100;
    long refill() default 100;
    long period() default 1;
    ChronoUnit unit() default ChronoUnit.SECONDS;
}
