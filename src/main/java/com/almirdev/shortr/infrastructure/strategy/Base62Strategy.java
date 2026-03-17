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

    /**
     * Offset added to every ID so even the first entries produce
     * 6-character codes instead of trivially short ones like "1".
     * 62^5 = 916,132,832 → any value above this yields ≥ 6 chars.
     */
    private static final long START_OFFSET = 1_000_000_000L;

    @Override
    public String generate(long id) {
        return encode(START_OFFSET + id);
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
