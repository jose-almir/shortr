package com.almirdev.shortr.application.service;

import com.almirdev.shortr.infrastructure.config.RetryConfig;
import com.almirdev.shortr.application.port.UrlCacheService;
import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {UrlRedirectServiceImpl.class, RetryConfig.class})
@ActiveProfiles("test")
class UrlRedirectServiceRetryTest {

    @Autowired
    private UrlRedirectService urlRedirectService;

    @MockitoBean
    private UrlRepository repository;

    @MockitoBean
    private UrlCacheService cacheService;

    @Test
    void shouldRetryOnRepositoryExceptionAndSucceed() {
        // Given
        String shortCode = "retry-me";
        String longUrl = "https://example.com";
        Url url = Url.builder().shortCode(shortCode).longUrl(longUrl).build();

        // Failure on first call, success on second
        when(cacheService.get(anyString())).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode))
                .thenThrow(new RuntimeException("Database timeout"))
                .thenReturn(Optional.of(url));

        // When
        String result = urlRedirectService.redirect(shortCode);

        // Then
        assertThat(result).isEqualTo(longUrl);
        // Repository should be called 2 times (1 fail, 1 success)
        verify(repository, times(2)).findByShortCode(shortCode);
        verify(cacheService, times(1)).set(anyString(), eq(longUrl), any());
    }

    @Test
    void shouldExhaustRetriesAndFail() {
        // Given
        String shortCode = "always-fail";
        when(cacheService.get(anyString())).thenReturn(Optional.empty());
        when(repository.findByShortCode(shortCode)).thenThrow(new RuntimeException("Persistent error"));

        // When & Then
        try {
            urlRedirectService.redirect(shortCode);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(RuntimeException.class);
            assertThat(e.getMessage()).isEqualTo("Persistent error");
        }

        // Repository should be called 3 times (default maxAttempts)
        verify(repository, times(3)).findByShortCode(shortCode);
    }
}
