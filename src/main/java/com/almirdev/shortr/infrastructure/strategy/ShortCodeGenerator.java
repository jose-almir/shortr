package com.almirdev.shortr.infrastructure.strategy;

import com.almirdev.shortr.domain.strategy.ShortCodeStrategy;
import org.springframework.stereotype.Component;

/**
 * Delegates short code generation to the active {@link ShortCodeStrategy}.
 */
@Component
public class ShortCodeGenerator {

    private final ShortCodeStrategy strategy;

    public ShortCodeGenerator(ShortCodeStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Generates a short code from a database-assigned entity ID.
     *
     * @param id The persisted entity ID.
     * @return The generated short code.
     */
    public String generate(long id) {
        return strategy.generate(id);
    }
}
