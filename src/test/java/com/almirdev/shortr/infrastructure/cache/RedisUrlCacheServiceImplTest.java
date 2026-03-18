package com.almirdev.shortr.infrastructure.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisUrlCacheServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisUrlCacheServiceImpl redisUrlCacheService;

    @BeforeEach
    void setUp() {
        // valueOperations needs to be returned by redisTemplate.opsForValue()
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnValueFromRedis() {
        // Given
        String key = "short-url";
        String expectedValue = "https://example.com";
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // When
        Optional<String> result = redisUrlCacheService.get(key);

        // Then
        assertThat(result).isPresent().contains(expectedValue);
        verify(valueOperations).get(key);
    }

    @Test
    void shouldReturnEmptyWhenKeyDoesNotExist() {
        // Given
        String key = "non-existent";
        when(valueOperations.get(key)).thenReturn(null);

        // When
        Optional<String> result = redisUrlCacheService.get(key);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenRedisFails() {
        // Given
        String key = "any-key";
        when(valueOperations.get(key)).thenThrow(new RedisConnectionFailureException("Redis is down"));

        // When
        Optional<String> result = redisUrlCacheService.get(key);

        // Then
        assertThat(result).isEmpty();
        // Exception should be caught and logged (verified by return value)
    }

    @Test
    void shouldStoreValueInRedis() {
        // Given
        String key = "short-url";
        String value = "https://example.com";
        Duration ttl = Duration.ofHours(1);

        // When
        redisUrlCacheService.set(key, value, ttl);

        // Then
        verify(valueOperations).set(key, value, ttl);
    }

    @Test
    void shouldNotThrowExceptionWhenSetFails() {
        // Given
        String key = "any-key";
        doThrow(new RedisConnectionFailureException("Redis is down"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When & Then
        redisUrlCacheService.set(key, "value", Duration.ofMinutes(1));
        // Verify no exception was bubbled up
        verify(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }
}
