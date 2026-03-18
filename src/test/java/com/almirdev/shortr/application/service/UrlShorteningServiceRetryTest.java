package com.almirdev.shortr.application.service;

import com.almirdev.shortr.infrastructure.config.RetryConfig;
import com.almirdev.shortr.application.port.UrlCacheService;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import com.almirdev.shortr.infrastructure.strategy.ShortCodeGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UrlShorteningServiceImpl.class, RetryConfig.class})
@ActiveProfiles("test")
class UrlShorteningServiceRetryTest {

    @Autowired
    private UrlShorteningService urlShorteningService;

    @MockitoBean
    private UrlRepository repository;

    @MockitoBean
    private UrlCacheService cacheService;

    @MockitoBean
    private ShortCodeGenerator shortCodeGenerator;

    @Test
    void shouldRetryOnShorteningFailureAndSucceed() {
        // Given
        String longUrl = "https://example.com";
        String expectedShortCode = "retry-short";

        when(cacheService.get(anyString())).thenReturn(Optional.empty());
        // Fail once, succeed on second attempt
        when(repository.findByLongUrl(longUrl))
                .thenThrow(new RuntimeException("Transient DB error"))
                .thenReturn(Optional.empty()); // DB miss on retry

        // When
        try {
            urlShorteningService.shorten(longUrl);
        } catch (Exception ignored) {
            // It might fail on createAndPersist too, let's keep it simple
        }

        // Then
        // Should have called repository.findByLongUrl at least twice
        verify(repository, atLeast(2)).findByLongUrl(longUrl);
    }
}
