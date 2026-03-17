package com.almirdev.shortr.domain.service;

public interface UrlShorteningService {
    /**
     * Shortens a long URL following the architecture logic.
     * @param longUrl The original URL.
     * @return The generated short code.
     */
    String shorten(String longUrl);
}
