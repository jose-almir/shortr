package com.almirdev.shortr.infrastructure.strategy;

import com.almirdev.shortr.domain.strategy.ShortCodeStrategy;
import org.springframework.stereotype.Component;

/**
 * Encodes the database-assigned ID into a compact Base62 string.
 * <p>
 * Because the ID is a unique, monotonically increasing sequence value,
 * the resulting short code is guaranteed collision-free without any
 * hashing step.
 */
@Component
public class Base62Strategy implements ShortCodeStrategy {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String generate(long id) {
        return encode(id);
    }

    private String encode(long value) {
        if (value == 0) {
            return String.valueOf(BASE62.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
