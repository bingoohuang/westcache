package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheConfigRegistry;
import com.github.bingoohuang.westcache.WestCacheFlusherRegistry;
import com.github.bingoohuang.westcache.WestCacheManagerRegistry;
import com.github.bingoohuang.westcache.WestCacheSnapshotRegistry;
import com.github.bingoohuang.westcache.base.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@Value @AllArgsConstructor
public class WestCacheOption {
    @Getter @Setter private final WestCacheFlusher flusher;
    @Getter @Setter private final WestCacheManager manager;
    @Getter @Setter private final WestCacheSnapshot snapshot;
    @Getter @Setter private final WestCacheConfig config;

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        WestCacheFlusher flusher = WestCacheFlusherRegistry.get("simple");
        WestCacheManager manager = WestCacheManagerRegistry.get("guava");
        WestCacheSnapshot snapshot = WestCacheSnapshotRegistry.get("none");
        WestCacheConfig config = WestCacheConfigRegistry.get("default");

        public Builder flusher(String flusherName) {
            this.flusher = WestCacheFlusherRegistry.get(flusherName);
            return this;
        }

        public Builder manager(String managerName) {
            this.manager = WestCacheManagerRegistry.get(managerName);
            return this;
        }

        public Builder snapshot(String snapshotName) {
            this.snapshot = WestCacheSnapshotRegistry.get(snapshotName);
            return this;
        }

        public Builder config(String configName) {
            this.config = WestCacheConfigRegistry.get(configName);
            return this;
        }

        public WestCacheOption build() {
            return new WestCacheOption(flusher, manager, snapshot, config);
        }

        public WestCacheOption build(WestCacheable westCacheable) {
            this.flusher = WestCacheFlusherRegistry.get(westCacheable.flusher());
            this.manager = WestCacheManagerRegistry.get(westCacheable.manager());
            this.snapshot = WestCacheSnapshotRegistry.get(westCacheable.snapshot());
            this.config = WestCacheConfigRegistry.get(westCacheable.config());
            return new WestCacheOption(flusher, manager, snapshot, config);
        }
    }
}
