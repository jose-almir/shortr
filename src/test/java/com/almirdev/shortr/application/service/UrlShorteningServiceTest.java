package com.almirdev.shortr.application.service;

import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.infrastructure.strategy.ShortCodeGenerator;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlShorteningServiceImpl service;

    private final String longUrl = "https://google.com";
    private final String shortCode = "abc12345";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldReturnCodeFromCacheWhenPresent() {
        when(valueOperations.get("long:" + longUrl)).thenReturn(shortCode);

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        verify(repository, never()).findByLongUrl(anyString());
    }

    @Test
    void shouldReturnCodeFromDbAndUpdateCacheWhenPresentInDb() {
        when(valueOperations.get("long:" + longUrl)).thenReturn(null);
        Url url = Url.builder().longUrl(longUrl).shortCode(shortCode).build();
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.of(url));

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        verify(valueOperations).set(eq("long:" + longUrl), eq(shortCode), any());
    }

    @Test
    void shouldGenerateNewCodeAndSaveWhenNotPresentAnywhere() {
        when(valueOperations.get("long:" + longUrl)).thenReturn(null);
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.empty());
        when(shortCodeGenerator.generate(longUrl)).thenReturn(shortCode);

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        verify(repository).save(any(Url.class));
        verify(valueOperations).set(eq("long:" + longUrl), eq(shortCode), any());
    }
}
