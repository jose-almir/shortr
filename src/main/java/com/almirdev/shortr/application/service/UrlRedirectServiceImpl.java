package com.almirdev.shortr.application.service;
 
import com.almirdev.shortr.application.port.UrlCacheService;
import com.almirdev.shortr.domain.exception.UrlNotFoundException;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import java.time.Duration;
 
@Service
@RequiredArgsConstructor
@Slf4j
public class UrlRedirectServiceImpl implements UrlRedirectService {
 
    private final UrlRepository repository;
    private final UrlCacheService cacheService;
 
    private static final String REDIRECT_CACHE_PREFIX = "short:"; // shortCode -> longUrl
    private static final Duration REDIRECT_CACHE_TTL = Duration.ofDays(7);
 
    /**
     * Redirects a short code to its corresponding long URL.
     * This method is retryable and has a timeout to ensure high availability and resistance 
     * against transient data source failures, providing a resilient experience for the end user.
     *
     * @param shortCode The encoded identifier for the short URL.
     * @return The original long URL.
     * @throws UrlNotFoundException If the short code does not exist.
     */
    @Override
    @Transactional(timeout = 7)
    @Retryable(
        retryFor = { Exception.class },
        backoff = @Backoff(delay = 1000)
    )
    public String redirect(String shortCode) {
        // 1. Lookup shortCode in Cache
        String cachedLongUrl = cacheService.get(REDIRECT_CACHE_PREFIX + shortCode).orElse(null);
        if (cachedLongUrl != null) {
            log.info("Cache hit for shortCode: {}", shortCode);
            return cachedLongUrl;
        }
 
        // 2. Cache Miss - Fetch from DB
        return repository.findByShortCode(shortCode)
                .map(url -> {
                    log.info("DB hit for shortCode: {}", shortCode);
                    // 3. Refill Cache
                    cacheService.set(REDIRECT_CACHE_PREFIX + shortCode, url.getLongUrl(), REDIRECT_CACHE_TTL);
                    return url.getLongUrl();
                })
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));
    }
}
