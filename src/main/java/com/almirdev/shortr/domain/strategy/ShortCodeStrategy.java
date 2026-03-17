package com.almirdev.shortr.domain.strategy;

public interface ShortCodeStrategy {
    /**
     * Generates a short code from a database-assigned unique ID.
     * Using the ID guarantees collision-free codes since the
     * underlying sequence is monotonically increasing.
     *
     * @param id The persisted entity ID.
     * @return A short code string derived from the ID.
     */
    String generate(long id);
}
