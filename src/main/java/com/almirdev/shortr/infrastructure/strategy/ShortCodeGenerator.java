package com.almirdev.shortr.infrastructure.strategy;

import com.almirdev.shortr.domain.strategy.ShortCodeStrategy;
import org.springframework.stereotype.Component;

@Component
public class ShortCodeGenerator {

    private final ShortCodeStrategy strategy;

    public ShortCodeGenerator(ShortCodeStrategy strategy) {
        this.strategy = strategy;
    }

    public String generate(String input) {
        return strategy.generate(input);
    }
}
