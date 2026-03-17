package com.almirdev.shortr.infrastructure.strategy;

import com.almirdev.shortr.domain.strategy.ShortCodeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ShortCodeGeneratorTest {

    private ShortCodeStrategy strategy;
    private ShortCodeGenerator generator;

    @BeforeEach
    void setUp() {
        strategy = Mockito.mock(ShortCodeStrategy.class);
        generator = new ShortCodeGenerator(strategy);
    }

    @Test
    void shouldDelegateToStrategy() {
        long id = 42L;
        String expectedCode = "abc12345";

        when(strategy.generate(id)).thenReturn(expectedCode);

        String result = generator.generate(id);

        assertEquals(expectedCode, result);
    }
}
