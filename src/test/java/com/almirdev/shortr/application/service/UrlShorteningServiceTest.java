package com.almirdev.shortr.application.service;

import com.almirdev.shortr.application.port.UrlCacheService;
import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.infrastructure.strategy.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShorteningServiceTest {

    @Mock
    private UrlRepository repository;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @Mock
    private UrlCacheService cacheService;

    @InjectMocks
    private UrlShorteningServiceImpl service;

    private final String longUrl = "https://google.com";
    private final String shortCode = "abc12345";

    @BeforeEach
    void setUp() {
        when(cacheService.get(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnCodeFromCacheWhenPresent() {
        when(cacheService.get(anyString())).thenReturn(Optional.of(shortCode));

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        verify(repository, never()).findByLongUrl(anyString());
    }

    @Test
    void shouldReturnCodeFromDbAndUpdateCacheWhenPresentInDb() {
        Url url = Url.builder().longUrl(longUrl).shortCode(shortCode).build();
        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.of(url));

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        verify(cacheService).set(eq("long:" + longUrl), eq(shortCode), any());
    }

    @Test
    void shouldGenerateNewCodeUsingSavedIdWhenNotPresentAnywhere() {
        long generatedId = 7L;

        when(repository.findByLongUrl(longUrl)).thenReturn(Optional.empty());

        // Simulate saveAndFlush assigning an ID to the entity
        doAnswer(invocation -> {
            Url entity = invocation.getArgument(0);
            entity.setId(generatedId);
            return entity;
        }).when(repository).saveAndFlush(any(Url.class));

        when(shortCodeGenerator.generate(generatedId)).thenReturn(shortCode);

        String result = service.shorten(longUrl);

        assertEquals(shortCode, result);
        // Phase 1: saveAndFlush to get ID
        verify(repository).saveAndFlush(any(Url.class));
        // Phase 2: save with the generated shortCode
        verify(repository).save(any(Url.class));
        // Cache populated
        verify(cacheService).set(eq("long:" + longUrl), eq(shortCode), any());
    }
}
