package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.WestCacheRegistry;
import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.base.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Value @AllArgsConstructor
public class WestCacheOption {
    @Getter private final WestCacheFlusher flusher;
    @Getter private final WestCacheManager manager;
    @Getter private final WestCacheSnapshot snapshot;
    @Getter private final WestCacheConfig config;
    @Getter private final WestCacheKeyer keyStrategy;
    @Getter private final String key;
    @Getter private final String specs;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        WestCacheFlusher flusher = WestCacheRegistry.getFlusher("simple");
        WestCacheManager manager = WestCacheRegistry.getManager("guava");
        WestCacheSnapshot snapshot = WestCacheRegistry.getSnapshot("none");
        WestCacheConfig config = WestCacheRegistry.getConfig("default");
        WestCacheKeyer keyStrategy = WestCacheRegistry.getKeyStrategy("default");
        String key = "";
        String specs = "";

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

        public Builder keyStrategy(String keyStrategyName) {
            this.keyStrategy = WestCacheRegistry.getKeyStrategy(keyStrategyName);
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder specs(String specs) {
            this.specs = specs;
            return this;
        }

        public WestCacheOption build() {
            return new WestCacheOption(flusher, manager, snapshot, config, keyStrategy, key, specs);
        }

        public WestCacheOption build(WestCacheable westCacheable) {
            this.flusher = WestCacheRegistry.getFlusher(westCacheable.flusher());
            this.manager = WestCacheRegistry.getManager(westCacheable.manager());
            this.snapshot = WestCacheRegistry.getSnapshot(westCacheable.snapshot());
            this.config = WestCacheRegistry.getConfig(westCacheable.config());
            this.keyStrategy = WestCacheRegistry.getKeyStrategy(westCacheable.keyer());
            this.key = westCacheable.key();
            this.specs = westCacheable.specs();
            return build();
        }
    }
}
