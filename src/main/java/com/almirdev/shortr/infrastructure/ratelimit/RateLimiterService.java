package com.almirdev.shortr.infrastructure.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;

import java.util.function.Supplier;

public class RateLimiterService {

    private final ProxyManager<String> proxyManager;

    public RateLimiterService(ProxyManager<String> proxyManager) {
        this.proxyManager = proxyManager;
    }

    public Bucket resolveBucket(String key, Supplier<BucketConfiguration> configurationSupplier) {
        return proxyManager.builder().build(key, configurationSupplier);
    }
}
