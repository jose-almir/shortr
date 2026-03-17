package com.almirdev.shortr.application.service;

import com.almirdev.shortr.domain.exception.UrlNotFoundException;
import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlRedirectServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UrlRedirectServiceImpl service;

    private final String shortCode = "abc12345";
    private final String longUrl = "https://google.com";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnUrlFromCacheWhenPresent() {
        when(valueOperations.get("short:" + shortCode)).thenReturn(longUrl);

        String result = service.redirect(shortCode);

        assertEquals(longUrl, result);
        verify(repository, never()).findByShortCode(anyString());
    }

    @Test
    void shouldReturnUrlFromDbAndRefillCacheWhenMissingInCache() {
        when(valueOperations.get("short:" + shortCode)).thenReturn(null);
        Url url = Url.builder().shortCode(shortCode).longUrl(longUrl).build();
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        String result = service.redirect(shortCode);

        assertEquals(longUrl, result);
        verify(valueOperations).set(eq("short:" + shortCode), eq(longUrl), any());
    }

    @Test
    void shouldThrowExceptionWhenNotFoundInDb() {
        when(valueOperations.get("short:" + shortCode)).thenReturn(null);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.redirect(shortCode));
    }
}
