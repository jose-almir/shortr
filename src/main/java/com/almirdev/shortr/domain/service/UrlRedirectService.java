package com.almirdev.shortr.domain.service;

public interface UrlRedirectService {
    /**
     * Finds the original long URL based on a short code.
     * @param shortCode The short code to look up.
     * @return The original long URL.
     * @throws com.almirdev.shortr.domain.exception.UrlNotFoundException if not found.
     */
    String redirect(String shortCode);
}
