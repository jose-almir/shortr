package com.almirdev.shortr.infrastructure.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62StrategyTest {

    private final Base62Strategy strategy = new Base62Strategy();

    @Test
    void shouldGenerateConsistentCodeForSameId() {
        String code1 = strategy.generate(42L);
        String code2 = strategy.generate(42L);

        assertEquals(code1, code2);
        assertNotNull(code1);
        assertFalse(code1.isEmpty());
    }

    @Test
    void shouldGenerateDifferentCodesForDifferentIds() {
        String code1 = strategy.generate(1L);
        String code2 = strategy.generate(2L);

        assertNotEquals(code1, code2);
    }

    @Test
    void shouldHandleEdgeCases() {
        assertDoesNotThrow(() -> strategy.generate(0L));
        assertDoesNotThrow(() -> strategy.generate(Long.MAX_VALUE));
    }
}
