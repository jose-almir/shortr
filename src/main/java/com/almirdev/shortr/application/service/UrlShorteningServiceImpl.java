package com.almirdev.shortr.application.service;
 
import com.almirdev.shortr.application.port.UrlCacheService;
import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import com.almirdev.shortr.infrastructure.strategy.ShortCodeGenerator;
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
public class UrlShorteningServiceImpl implements UrlShorteningService {
 
    private final UrlRepository repository;
    private final UrlCacheService cacheService;
    private final ShortCodeGenerator shortCodeGenerator;
 
    private static final String CREATE_CACHE_PREFIX = "long:"; // longUrl -> shortCode
    private static final Duration CREATE_CACHE_TTL = Duration.ofHours(24);
 
    /**
     * Shortens a given long URL, implementing a cache-miss pattern with DB fallback.
     * It uses retry logic to handle potential deadlocks or transient issues during 
     * the creation of new short codes, guaranteeing data consistency.
     *
     * @param longUrl The original URL to be shortened.
     * @return The generated short code.
     */
    @Override
    @Transactional(timeout = 7)
    @Retryable(
        retryFor = { Exception.class },
        backoff = @Backoff(delay = 1000)
    )
    public String shorten(String longUrl) {
        // 1. Check Cache (longUrl -> shortCode)
        String cachedShortCode = cacheService.get(CREATE_CACHE_PREFIX + longUrl).orElse(null);
        if (cachedShortCode != null) {
            log.info("Cache hit for longUrl: {}", longUrl);
            return cachedShortCode;
        }
 
        // 2. Cache Miss - Check DB
        return repository.findByLongUrl(longUrl)
                .map(url -> {
                    log.info("DB hit for longUrl: {}", longUrl);
                    cacheShortCode(longUrl, url.getShortCode());
                    return url.getShortCode();
                })
                .orElseGet(() -> createAndPersist(longUrl));
    }
 
    /**
     * Two-phase save: insert the entity first to obtain the database-
     * generated ID, then derive the short code from that ID and update.
     * This guarantees collision-free codes since the ID sequence is unique.
     */
    private String createAndPersist(String longUrl) {
        log.info("Generating new short code for longUrl: {}", longUrl);
 
        // Phase 1 — persist to obtain the auto-generated ID
        Url url = Url.builder()
                .longUrl(longUrl)
                .build();
        repository.saveAndFlush(url);
 
        // Phase 2 — derive short code from the unique ID
        String shortCode = shortCodeGenerator.generate(url.getId());
        url.setShortCode(shortCode);
        repository.save(url);
 
        cacheShortCode(longUrl, shortCode);
        return shortCode;
    }
 
    private void cacheShortCode(String longUrl, String shortCode) {
        cacheService.set(CREATE_CACHE_PREFIX + longUrl, shortCode, CREATE_CACHE_TTL);
    }
}
