package com.almirdev.shortr.application.service;

import com.almirdev.shortr.domain.model.Url;
import com.almirdev.shortr.domain.repository.UrlRepository;
import com.almirdev.shortr.domain.service.UrlShorteningService;
import com.almirdev.shortr.infrastructure.strategy.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShorteningServiceImpl implements UrlShorteningService {

    private final UrlRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ShortCodeGenerator shortCodeGenerator;

    private static final String CREATE_CACHE_PREFIX = "long:"; // longUrl -> shortCode
    private static final Duration CREATE_CACHE_TTL = Duration.ofHours(24);

    @Override
    public String shorten(String longUrl) {
        // 1. Check Cache (longUrl -> shortCode)
        String cachedShortCode = redisTemplate.opsForValue().get(CREATE_CACHE_PREFIX + longUrl);
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
                .orElseGet(() -> {
                    // 3. Not in DB - Generate and Insert
                    log.info("Generating new short code for longUrl: {}", longUrl);
                    String shortCode = shortCodeGenerator.generate(longUrl);
                    
                    Url url = Url.builder()
                            .longUrl(longUrl)
                            .shortCode(shortCode)
                            .build();
                    
                    repository.save(url);
                    cacheShortCode(longUrl, shortCode);
                    return shortCode;
                });
    }

    private void cacheShortCode(String longUrl, String shortCode) {
        redisTemplate.opsForValue().set(CREATE_CACHE_PREFIX + longUrl, shortCode, CREATE_CACHE_TTL);
    }
}
