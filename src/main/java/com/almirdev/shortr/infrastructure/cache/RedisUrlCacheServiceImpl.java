package com.almirdev.shortr.infrastructure.cache;

import com.almirdev.shortr.application.port.UrlCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Implementation of {@link UrlCacheService} using Redis.
 * This implementation is designed to be fault-tolerant; if Redis is unavailable,
 * errors are caught and logged, but they do not block the application.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisUrlCacheServiceImpl implements UrlCacheService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Retrieves a value from the cache for the given key.
     * This operation is wrapped in a try-catch block to ensure that a failure in the 
     * caching infrastructure (e.g., Redis down) does not crash the entire application flow, 
     * instead falling back to the primary data source for maximum resilience.
     *
     * @param key The unique identifier for the cached URL.
     * @return An Optional containing the value, or empty if not found or if an error occurred.
     */
    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("Failed to retrieve key '{}' from Redis. Falling back to DB.", key, e);
            return Optional.empty();
        }
    }

    /**
     * Stores a key-value pair in the cache with a specified TTL.
     * This method silently catches exceptions because caching is an optimization, 
     * not a critical requirement; the application flow should proceed even if the 
     * cache is momentarily unavailable.
     *
     * @param key   The key to store.
     * @param value The value to associate with the key.
     * @param ttl   The time-to-live duration.
     */
    @Override
    public void set(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.error("Failed to store key '{}' in Redis. Continuing without caching.", key, e);
        }
    }
}
