package com.almirdev.shortr.infrastructure.strategy;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RandomStrategyTest {

    private final RandomStrategy strategy = new RandomStrategy();

    @Test
    void shouldGenerateCodeOfFixedLength() {
        String code = strategy.generate("any-input");
        assertEquals(8, code.length());
    }

    @Test
    void shouldGenerateDifferentCodesForSameInput() {
        String input = "test";
        String code1 = strategy.generate(input);
        String code2 = strategy.generate(input);
        
        assertNotEquals(code1, code2);
    }
}
