package com.almirdev.shortr.infrastructure.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Base62StrategyTest {

    private final Base62Strategy strategy = new Base62Strategy();

    @Test
    void shouldGenerateConsistentCodeForSameInput() {
        String input = "https://google.com";
        String code1 = strategy.generate(input);
        String code2 = strategy.generate(input);
        
        assertEquals(code1, code2);
        assertNotNull(code1);
        assertFalse(code1.isEmpty());
    }

    @Test
    void shouldGenerateCodeWithReasonableLength() {
        String code = strategy.generate("https://google.com");
        // Base62 from 8 bytes of hash should be around 11-12 chars max
        assertTrue(code.length() > 5);

    }
}
