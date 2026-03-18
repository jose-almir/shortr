package com.almirdev.shortr.application.port;

import java.time.Duration;
import java.util.Optional;

/**
 * Port for URL caching operations.
 * Defines the contract for storing and retrieving shortened URL mappings.
 */
public interface UrlCacheService {

    /**
     * Retrieves a value from the cache.
     *
     * @param key The cache key.
     * @return An Optional containing the value if present, or empty if not found or if cache is unavailable.
     */
    Optional<String> get(String key);

    /**
     * Stores a value in the cache with a Time-To-Live (TTL).
     *
     * @param key   The cache key.
     * @param value The value to store.
     * @param ttl   The duration for which the entry should remain in the cache.
     */
    void set(String key, String value, Duration ttl);
}
