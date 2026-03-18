package com.almirdev.shortr.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration to enable Spring Retry functionality.
 */
@Configuration
@EnableRetry
public class RetryConfig {
}
