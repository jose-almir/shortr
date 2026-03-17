package com.almirdev.shortr.application.service;

import com.almirdev.shortr.domain.exception.UrlNotFoundException;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlRedirectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlRedirectServiceImpl implements UrlRedirectService {

    private final UrlRepository repository;
    private final StringRedisTemplate redisTemplate;

    private static final String REDIRECT_CACHE_PREFIX = "short:"; // shortCode -> longUrl
    private static final Duration REDIRECT_CACHE_TTL = Duration.ofDays(7);

    @Override
    public String redirect(String shortCode) {
        // 1. Lookup shortCode in Cache
        String cachedLongUrl = redisTemplate.opsForValue().get(REDIRECT_CACHE_PREFIX + shortCode);
        if (cachedLongUrl != null) {
            log.info("Cache hit for shortCode: {}", shortCode);
            return cachedLongUrl;
        }

        // 2. Cache Miss - Fetch from DB
        return repository.findByShortCode(shortCode)
                .map(url -> {
                    log.info("DB hit for shortCode: {}", shortCode);
                    // 3. Refill Cache
                    redisTemplate.opsForValue().set(REDIRECT_CACHE_PREFIX + shortCode, url.getLongUrl(), REDIRECT_CACHE_TTL);
                    return url.getLongUrl();
                })
                .orElseThrow(() -> new UrlNotFoundException("Short code not found: " + shortCode));
    }
}
