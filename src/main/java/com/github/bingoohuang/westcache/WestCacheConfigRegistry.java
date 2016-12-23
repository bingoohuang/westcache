package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheConfig;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheConfigRegistry {
    Cache<String, WestCacheConfig> registry = CacheBuilder.newBuilder().build();

    static {
        registerConfig("default", new DefaultWestCacheConfig());
    }

    public void registerConfig(String configName, WestCacheConfig config) {
        WestCacheConfig cachedConfig = registry.getIfPresent(configName);
        if (cachedConfig != null) throw new RuntimeException(
                "cache config name " + cachedConfig + " already exists");

        registry.put(configName, config);
    }

    public void deregisterConfig(String configName) {
        registry.invalidate(configName);
    }

    public WestCacheConfig get(String configName) {
        return registry.getIfPresent(configName);
    }
}
