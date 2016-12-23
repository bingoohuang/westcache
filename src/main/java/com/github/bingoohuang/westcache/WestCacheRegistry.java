package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.cachekey.DefaultKeyStrategy;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.NoneCacheFlusher;
import com.github.bingoohuang.westcache.flusher.SimpleCacheFlusher;
import com.github.bingoohuang.westcache.manager.GuavaCacheManager;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheRegistry {
    Cache<String, WestCacheConfig> configRegistry = CacheBuilder.newBuilder().build();

    static {
        registerConfig("default", new DefaultWestCacheConfig());
    }

    public void registerConfig(String configName, WestCacheConfig config) {
        WestCacheConfig cachedConfig = configRegistry.getIfPresent(configName);
        if (cachedConfig != null) throw new RuntimeException(
                "cache config name " + cachedConfig + " already exists");

        configRegistry.put(configName, config);
    }

    public void deregisterConfig(String configName) {
        configRegistry.invalidate(configName);
    }

    public WestCacheConfig getConfig(String configName) {
        return configRegistry.getIfPresent(configName);
    }

    Cache<String, WestCacheFlusher> flusherRegistry = CacheBuilder.newBuilder().build();

    static {
        registerFlusher("none", new NoneCacheFlusher());
        registerFlusher("simple", new SimpleCacheFlusher());
    }

    public void registerFlusher(String flusherName, WestCacheFlusher flusher) {
        WestCacheFlusher cacheFlusher = flusherRegistry.getIfPresent(flusherName);
        if (cacheFlusher != null) throw new RuntimeException(
                "flush name " + flusherName + " already exists");

        flusherRegistry.put(flusherName, flusher);
    }

    public void deregisterFlusher(String flusherName) {
        flusherRegistry.invalidate(flusherName);
    }

    public WestCacheFlusher getFlusher(String flusherName) {
        return flusherRegistry.getIfPresent(flusherName);
    }

    public void flush(WestCacheOptions option, Object bean, String methodName, Object... args) {
        String cacheKey = option.getKeyStrategy().getCacheKey(option, methodName, bean, args);
        option.getFlusher().flush(cacheKey);
    }


    Cache<String, WestCacheManager> managerRegistry = CacheBuilder.newBuilder().build();

    static {
        registerManger("guava", new GuavaCacheManager());
    }

    public void registerManger(String managerName, WestCacheManager manager) {
        WestCacheManager cachedManager = managerRegistry.getIfPresent(managerName);
        if (cachedManager != null) throw new RuntimeException(
                "cache manager name " + cachedManager + " already exists");

        managerRegistry.put(managerName, manager);
    }

    public void deregisterManger(String managerName) {
        managerRegistry.invalidate(managerName);
    }

    public WestCacheManager getManager(String managerName) {
        return managerRegistry.getIfPresent(managerName);
    }

    Cache<String, WestCacheSnapshot> snapshotRegistry = CacheBuilder.newBuilder().build();

    static {
        registerSnapshot("file", new FileCacheSnapshot());
    }

    public void registerSnapshot(String snapshotName, WestCacheSnapshot snapshot) {
        WestCacheSnapshot cacheSnapshot = snapshotRegistry.getIfPresent(snapshotName);
        if (cacheSnapshot != null) throw new RuntimeException(
                "snapshot name " + snapshotName + " already exists");

        snapshotRegistry.put(snapshotName, snapshot);
    }

    public void deregisterSnapshot(String snapshotName) {
        snapshotRegistry.invalidate(snapshotName);
    }

    public WestCacheSnapshot getSnapshot(String snapshotName) {
        return snapshotRegistry.getIfPresent(snapshotName);
    }

    Cache<String, WestCacheKeyStrategy> keyStrategyRegistry = CacheBuilder.newBuilder().build();

    static {
        registerKeyStrategy("default", new DefaultKeyStrategy());
    }

    public void registerKeyStrategy(String keyStrategyName, WestCacheKeyStrategy keyStrategy) {
        val cacheKeyStrategy = keyStrategyRegistry.getIfPresent(keyStrategyName);
        if (cacheKeyStrategy != null) throw new RuntimeException(
                "key-strategy name " + keyStrategyName + " already exists");

        keyStrategyRegistry.put(keyStrategyName, keyStrategy);
    }

    public void deregisterKeyStrategy(String keyStrategyName) {
        keyStrategyRegistry.invalidate(keyStrategyName);
    }

    public WestCacheKeyStrategy getKeyStrategy(String keyStrategyName) {
        return keyStrategyRegistry.getIfPresent(keyStrategyName);
    }
}
