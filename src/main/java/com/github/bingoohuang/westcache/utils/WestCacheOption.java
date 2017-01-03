package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.base.*;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Map;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Value @AllArgsConstructor
public class WestCacheOption {
    @Getter private final WestCacheFlusher flusher;
    @Getter private final WestCacheManager manager;
    @Getter private final WestCacheSnapshot snapshot;
    @Getter private final WestCacheConfig config;
    @Getter private final WestCacheInterceptor interceptor;
    @Getter private final WestCacheKeyer keyStrategy;
    @Getter private final String key;
    @Getter private final Map<String, String> specs;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        WestCacheFlusher flusher = WestCacheRegistry.getFlusher("simple");
        WestCacheManager manager = WestCacheRegistry.getManager("guava");
        WestCacheSnapshot snapshot = WestCacheRegistry.getSnapshot("bypass");
        WestCacheConfig config = WestCacheRegistry.getConfig("default");
        WestCacheInterceptor interceptor = WestCacheRegistry.getInterceptor("bypass");
        WestCacheKeyer keyStrategy = WestCacheRegistry.getKeyStrategy("default");
        String key = "";
        Map<String, String> specs = Maps.newHashMap();

        public Builder flusher(String flusherName) {
            this.flusher = WestCacheRegistry.getFlusher(flusherName);
            return this;
        }

        public Builder manager(String managerName) {
            this.manager = WestCacheRegistry.getManager(managerName);
            return this;
        }

        public Builder snapshot(String snapshotName) {
            this.snapshot = WestCacheRegistry.getSnapshot(snapshotName);
            return this;
        }

        public Builder config(String configName) {
            this.config = WestCacheRegistry.getConfig(configName);
            return this;
        }

        public Builder interceptor(String interceptorName) {
            this.interceptor = WestCacheRegistry.getInterceptor(interceptorName);
            return this;
        }

        public Builder keyStrategy(String keyStrategyName) {
            this.keyStrategy = WestCacheRegistry.getKeyStrategy(keyStrategyName);
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder specs(String specs) {
            this.specs = Specs.parseSpecs(specs);
            return this;
        }

        public Builder specs(Map<String, String> specs) {
            this.specs = specs;
            return this;
        }

        public WestCacheOption build() {
            return new WestCacheOption(flusher, manager, snapshot,
                    config, interceptor, keyStrategy, key, specs);
        }

        public WestCacheOption build(WestCacheable westCacheable) {
            this.flusher = WestCacheRegistry.getFlusher(westCacheable.flusher());
            this.manager = WestCacheRegistry.getManager(westCacheable.manager());
            this.snapshot = WestCacheRegistry.getSnapshot(westCacheable.snapshot());
            this.config = WestCacheRegistry.getConfig(westCacheable.config());
            this.interceptor = WestCacheRegistry.getInterceptor(westCacheable.interceptor());
            this.keyStrategy = WestCacheRegistry.getKeyStrategy(westCacheable.keyer());
            this.key = westCacheable.key();
            this.specs = Specs.parseSpecs(westCacheable.specs());
            return build();
        }
    }
}
