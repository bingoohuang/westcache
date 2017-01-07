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
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class WestCacheRegistry {
    public final  static RegistryTemplate<WestCacheConfig> configRegistry
            = new RegistryTemplate<WestCacheConfig>();

    static {
        configRegistry.register("default", new DefaultWestCacheConfig());
    }

    public final static RegistryTemplate<WestCacheFlusher> flusherRegistry
            = new RegistryTemplate<WestCacheFlusher>();

    static {
        flusherRegistry.register("default", new ByPassCacheFlusher());
        flusherRegistry.register("simple", new SimpleCacheFlusher());
        flusherRegistry.register("diamond", new DiamondCacheFlusher());
        flusherRegistry.register("table", new TableCacheFlusher());
    }

    public static void flush(WestCacheOption option,
                             Object bean,
                             String methodName,
                             Object... args) {
        val keyer = option.getKeyer();
        val cacheKey = keyer.getCacheKey(option, methodName, bean, args);
        option.getFlusher().flush(option, cacheKey, "");
    }

    public final static RegistryTemplate<WestCacheManager> managerRegistry
            = new RegistryTemplate<WestCacheManager>();

    static {
        managerRegistry.register("default", new GuavaCacheManager());
        managerRegistry.register("diamond", new DiamondCacheManager());
        managerRegistry.register("file", new FileCacheManager());
        managerRegistry.register("expiring", new ExpiringMapCacheManager());
        managerRegistry.register("redis", new RedisCacheManager());
    }

    public final static RegistryTemplate<WestCacheSnapshot> snapshotRegistry
            = new RegistryTemplate<WestCacheSnapshot>();

    static {
        snapshotRegistry.register("file", new FileCacheSnapshot());
        snapshotRegistry.register("redis", new RedisCacheSnapshot());
    }

    public final static RegistryTemplate<WestCacheKeyer> keyerRegistry
            = new RegistryTemplate<WestCacheKeyer>();

    static {
        keyerRegistry.register("default", new DefaultKeyer());
        keyerRegistry.register("simple", new SimpleKeyer());
        keyerRegistry.register("packagelimit", new PackageLimitedKeyer());
    }

    public final static RegistryTemplate<WestCacheInterceptor> interceptorRegistry
            = new RegistryTemplate<WestCacheInterceptor>();

    static {
        interceptorRegistry.register("default", new ByPassInterceptor());
        interceptorRegistry.register("redis", new RedisInterceptor());
    }
}
