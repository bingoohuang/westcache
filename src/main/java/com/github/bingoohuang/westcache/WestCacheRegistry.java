package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.cachekey.DefaultKeyStrategy;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.NoneCacheFlusher;
import com.github.bingoohuang.westcache.flusher.SimpleCacheFlusher;
import com.github.bingoohuang.westcache.manager.GuavaCacheManager;
import com.github.bingoohuang.westcache.registry.RegistryTemplate;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheRegistry {
    RegistryTemplate<WestCacheConfig> configRegistry = new RegistryTemplate<WestCacheConfig>();

    static {
        registerConfig("default", new DefaultWestCacheConfig());
    }

    public void registerConfig(String configName, WestCacheConfig config) {
        configRegistry.register(configName, config);
    }

    public void deregisterConfig(String configName) {
        configRegistry.deregister(configName);
    }

    public WestCacheConfig getConfig(String configName) {
        return configRegistry.get(configName);
    }

    RegistryTemplate<WestCacheFlusher> flusherRegistry = new RegistryTemplate<WestCacheFlusher>();

    static {
        registerFlusher("none", new NoneCacheFlusher());
        registerFlusher("simple", new SimpleCacheFlusher());
    }

    public void registerFlusher(String flusherName, WestCacheFlusher flusher) {
        flusherRegistry.register(flusherName, flusher);
    }

    public void deregisterFlusher(String flusherName) {
        flusherRegistry.deregister(flusherName);
    }

    public WestCacheFlusher getFlusher(String flusherName) {
        return flusherRegistry.get(flusherName);
    }

    public void flush(WestCacheOptions option,
                      Object bean,
                      String methodName,
                      Object... args) {
        val keyStrategy = option.getKeyStrategy();
        String cacheKey = keyStrategy.getCacheKey(option, methodName, bean, args);
        option.getFlusher().flush(cacheKey);
    }

    RegistryTemplate<WestCacheManager> managerRegistry = new RegistryTemplate<WestCacheManager>();

    static {
        registerManger("guava", new GuavaCacheManager());
    }

    public void registerManger(String managerName, WestCacheManager manager) {
        managerRegistry.register(managerName, manager);
    }

    public void deregisterManger(String managerName) {
        managerRegistry.deregister(managerName);
    }

    public WestCacheManager getManager(String managerName) {
        return managerRegistry.get(managerName);
    }

    RegistryTemplate<WestCacheSnapshot> snapshotRegistry = new RegistryTemplate<WestCacheSnapshot>();

    static {
        registerSnapshot("file", new FileCacheSnapshot());
    }

    public void registerSnapshot(String snapshotName, WestCacheSnapshot snapshot) {
        snapshotRegistry.register(snapshotName, snapshot);
    }

    public void deregisterSnapshot(String snapshotName) {
        snapshotRegistry.deregister(snapshotName);
    }

    public WestCacheSnapshot getSnapshot(String snapshotName) {
        return snapshotRegistry.get(snapshotName);
    }

    RegistryTemplate<WestCacheKeyStrategy> keyStrategyRegistry = new RegistryTemplate<WestCacheKeyStrategy>();

    static {
        registerKeyStrategy("default", new DefaultKeyStrategy());
    }

    public void registerKeyStrategy(String keyStrategyName, WestCacheKeyStrategy keyStrategy) {
        keyStrategyRegistry.register(keyStrategyName, keyStrategy);
    }

    public void deregisterKeyStrategy(String keyStrategyName) {
        keyStrategyRegistry.deregister(keyStrategyName);
    }

    public WestCacheKeyStrategy getKeyStrategy(String keyStrategyName) {
        return keyStrategyRegistry.get(keyStrategyName);
    }
}
