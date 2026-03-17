package com.almirdev.shortr.domain.strategy;

public interface ShortCodeStrategy {
    /**
     * Generates a short code based on the input string.
     * @param input The original data to base the hash or generation on.
     * @return A unique short code string.
     */
    String generate(String input);
}
