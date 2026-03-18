package com.almirdev.shortr.application.service;

import com.almirdev.shortr.application.port.UrlCacheService;
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
    private UrlCacheService cacheService;

    @InjectMocks
    private UrlRedirectServiceImpl service;

    private final String shortCode = "abc12345";
    private final String longUrl = "https://google.com";

    @BeforeEach
    void setUp() {
        when(cacheService.get(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnUrlFromCacheWhenPresent() {
        when(cacheService.get(anyString())).thenReturn(Optional.of(longUrl));

        String result = service.redirect(shortCode);

        assertEquals(longUrl, result);
        verify(repository, never()).findByShortCode(anyString());
    }

    @Test
    void shouldReturnUrlFromDbAndRefillCacheWhenMissingInCache() {
        Url url = Url.builder().shortCode(shortCode).longUrl(longUrl).build();
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(url));

        String result = service.redirect(shortCode);

        assertEquals(longUrl, result);
        verify(cacheService).set(eq("short:" + shortCode), eq(longUrl), any());
    }

    @Test
    void shouldThrowExceptionWhenNotFoundInDb() {
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> service.redirect(shortCode));
    }
}
