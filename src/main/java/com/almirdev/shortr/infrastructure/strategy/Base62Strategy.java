package com.almirdev.shortr.infrastructure.strategy;

import com.almirdev.shortr.domain.strategy.ShortCodeStrategy;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component("base62")
@ConditionalOnProperty(name = "shortr.strategy", havingValue = "base62", matchIfMissing = true)
public class Base62Strategy implements ShortCodeStrategy {

    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Override
    public String generate(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // To keep it short and reliable, we'll use a portion of the MD5 
            // and convert to Base62
            long value = 0;
            for (int i = 0; i < 8; i++) {
                value = (value << 8) | (hashBytes[i] & 0xFF);
            }
            
            return encode(value);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating short code: MD5 algorithm not found", e);
        }
    }

    private String encode(long value) {
        StringBuilder sb = new StringBuilder();
        value = Math.abs(value);
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }
}
