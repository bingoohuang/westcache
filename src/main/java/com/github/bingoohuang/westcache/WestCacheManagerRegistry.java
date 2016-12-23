package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheManager;
import com.github.bingoohuang.westcache.manager.GuavaCacheManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheManagerRegistry {
    Cache<String, WestCacheManager> registry = CacheBuilder.newBuilder().build();

    static {
        registerManger("guava", new GuavaCacheManager());
    }

    public void registerManger(String managerName, WestCacheManager manager) {
        WestCacheManager cachedManager = registry.getIfPresent(managerName);
        if (cachedManager != null) throw new RuntimeException(
                "cache manager name " + cachedManager + " already exists");

        registry.put(managerName, manager);
    }

    public void deregisterManger(String managerName) {
        registry.invalidate(managerName);
    }

    public WestCacheManager get(String managerName) {
        return registry.getIfPresent(managerName);
    }

}
