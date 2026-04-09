package com.almirdev.shortr.infrastructure.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.apache.catalina.util.RateLimiter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RateLimitConfig {
    @Bean
    public ProxyManager<String> proxyManager(RedisConnectionFactory redisConnectionFactory) {
        LettuceConnectionFactory lettuceConnectionFactory = (LettuceConnectionFactory) redisConnectionFactory;
        RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();

        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        ExpirationAfterWriteStrategy strategy = ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1));

        return Bucket4jLettuce.casBasedBuilder(connection)
                .expirationAfterWrite(strategy)
                .build();
    }

    @Bean
    public RateLimiterService rateLimiterService(ProxyManager<String> proxyManager) {
        return new RateLimiterService(proxyManager);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
    public RateLimitAspect rateLimiter(RateLimiterService rateLimiterService, RateLimitKeyResolver rateLimitKeyResolver) {
        return new RateLimitAspect(rateLimiterService, rateLimitKeyResolver);
    }
}
