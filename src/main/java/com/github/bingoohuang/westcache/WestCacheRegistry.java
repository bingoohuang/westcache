package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.ByPassCacheFlusher;
import com.github.bingoohuang.westcache.flusher.DiamondCacheFlusher;
import com.github.bingoohuang.westcache.flusher.SimpleCacheFlusher;
import com.github.bingoohuang.westcache.interceptor.ByPassInterceptor;
import com.github.bingoohuang.westcache.interceptor.RedisInterceptor;
import com.github.bingoohuang.westcache.keyer.DefaultKeyer;
import com.github.bingoohuang.westcache.keyer.SimpleKeyer;
import com.github.bingoohuang.westcache.manager.*;
import com.github.bingoohuang.westcache.outofbox.PackageLimitedKeyer;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.registry.RegistryTemplate;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.github.bingoohuang.westcache.snapshot.RedisCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheRegistry {
    RegistryTemplate<WestCacheConfig> configRegistry
            = new RegistryTemplate<WestCacheConfig>();

    static {
        register("default", new DefaultWestCacheConfig());
    }

    public void register(String configName, WestCacheConfig config) {
        configRegistry.register(configName, config);
    }

    public void deregisterConfig(String configName) {
        configRegistry.deregister(configName);
    }

    public WestCacheConfig getConfig(String configName) {
        return configRegistry.get(configName);
    }

    RegistryTemplate<WestCacheFlusher> flusherRegistry
            = new RegistryTemplate<WestCacheFlusher>();

    static {
        register("bypass", new ByPassCacheFlusher());
        register("simple", new SimpleCacheFlusher());
        register("diamond", new DiamondCacheFlusher());
        register("table", new TableCacheFlusher());
    }

    public void register(String flusherName, WestCacheFlusher flusher) {
        flusherRegistry.register(flusherName, flusher);
    }

    public void deregisterFlusher(String flusherName) {
        flusherRegistry.deregister(flusherName);
    }

    public WestCacheFlusher getFlusher(String flusherName) {
        return flusherRegistry.get(flusherName);
    }

    public void flush(WestCacheOption option,
                      Object bean,
                      String methodName,
                      Object... args) {
        val keyStrategy = option.getKeyer();
        val cacheKey = keyStrategy.getCacheKey(option, methodName, bean, args);
        option.getFlusher().flush(option, cacheKey);
    }

    RegistryTemplate<WestCacheManager> managerRegistry
            = new RegistryTemplate<WestCacheManager>();

    static {
        register("guava", new GuavaCacheManager());
        register("diamond", new DiamondCacheManager());
        register("file", new FileCacheManager());
        register("expiring", new ExpiringMapCacheManager());
        register("redis", new RedisCacheManager());
    }

    public void register(String managerName, WestCacheManager manager) {
        managerRegistry.register(managerName, manager);
    }

    public void deregisterManager(String managerName) {
        managerRegistry.deregister(managerName);
    }

    public WestCacheManager getManager(String managerName) {
        return managerRegistry.get(managerName);
    }

    RegistryTemplate<WestCacheSnapshot> snapshotRegistry
            = new RegistryTemplate<WestCacheSnapshot>();

    static {
        register("file", new FileCacheSnapshot());
        register("redis", new RedisCacheSnapshot());
    }

    public void register(String snapshotName, WestCacheSnapshot snapshot) {
        snapshotRegistry.register(snapshotName, snapshot);
    }

    public void deregisterSnapshot(String snapshotName) {
        snapshotRegistry.deregister(snapshotName);
    }

    public WestCacheSnapshot getSnapshot(String snapshotName) {
        return snapshotRegistry.get(snapshotName);
    }

    RegistryTemplate<WestCacheKeyer> keyStrategyRegistry
            = new RegistryTemplate<WestCacheKeyer>();

    static {
        register("default", new DefaultKeyer());
        register("simple", new SimpleKeyer());
        register("packagelimit", new PackageLimitedKeyer());
    }

    public void register(String keyStrategyName, WestCacheKeyer keyStrategy) {
        keyStrategyRegistry.register(keyStrategyName, keyStrategy);
    }

    public void deregisterKeyStrategy(String keyStrategyName) {
        keyStrategyRegistry.deregister(keyStrategyName);
    }

    public WestCacheKeyer getKeyer(String keyStrategyName) {
        return keyStrategyRegistry.get(keyStrategyName);
    }

    RegistryTemplate<WestCacheInterceptor> interceptorRegistry
            = new RegistryTemplate<WestCacheInterceptor>();

    static {
        register("bypass", new ByPassInterceptor());
        register("redis", new RedisInterceptor());
    }

    public void register(String interceptorName, WestCacheInterceptor keyStrategy) {
        interceptorRegistry.register(interceptorName, keyStrategy);
    }

    public void deregisterInterceptor(String interceptorName) {
        interceptorRegistry.deregister(interceptorName);
    }

    public WestCacheInterceptor getInterceptor(String interceptorName) {
        return interceptorRegistry.get(interceptorName);
    }
}
