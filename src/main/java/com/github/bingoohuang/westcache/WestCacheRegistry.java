package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.*;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.ByPassCacheFlusher;
import com.github.bingoohuang.westcache.flusher.DiamondCacheFlusher;
import com.github.bingoohuang.westcache.flusher.QuartzCacheFlusher;
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
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public abstract class WestCacheRegistry {
    public static final RegistryTemplate<WestCacheConfig> REGISTRY_TEMPLATE
            = new RegistryTemplate<WestCacheConfig>();

    static {
        REGISTRY_TEMPLATE.register("default", new DefaultWestCacheConfig());
    }

    public static final RegistryTemplate<WestCacheFlusher> FLUSHER_REGISTRY
            = new RegistryTemplate<WestCacheFlusher>();

    static {
        FLUSHER_REGISTRY.register("default", new ByPassCacheFlusher());
        FLUSHER_REGISTRY.register("simple", new SimpleCacheFlusher());
        if (Envs.HAS_DIAMOND) FLUSHER_REGISTRY.register("diamond", new DiamondCacheFlusher());
        if (Envs.HAS_EQL) FLUSHER_REGISTRY.register("table", new TableCacheFlusher());
        if (Envs.HAS_QUARTZ) FLUSHER_REGISTRY.register("quartz", new QuartzCacheFlusher());
    }


    public static void flush(WestCacheOption option,
                             Object bean,
                             String methodName,
                             Object... args) {
        val keyer = option.getKeyer();
        val cacheKey = keyer.getCacheKey(option, methodName, bean, args);
        option.getFlusher().flush(option, cacheKey, "");
    }

    public static final RegistryTemplate<WestCacheManager> MANAGER_REGISTRY
            = new RegistryTemplate<WestCacheManager>();

    static {
        MANAGER_REGISTRY.register("default", new GuavaCacheManager());
        MANAGER_REGISTRY.register("file", new FileCacheManager());
        if (Envs.HAS_DIAMOND) MANAGER_REGISTRY.register("diamond", new DiamondCacheManager());
        if (Envs.HAS_EXPIRING) MANAGER_REGISTRY.register("expiring", new ExpiringMapCacheManager());
        if (Envs.HAS_JEDIS) MANAGER_REGISTRY.register("redis", new RedisCacheManager());
    }

    public static final RegistryTemplate<WestCacheSnapshot> SNAPSHOT_REGISTRY
            = new RegistryTemplate<WestCacheSnapshot>();

    static {
        SNAPSHOT_REGISTRY.register("file", new FileCacheSnapshot());
        if (Envs.HAS_JEDIS) SNAPSHOT_REGISTRY.register("redis", new RedisCacheSnapshot());
    }

    public static final RegistryTemplate<WestCacheKeyer> KEYER_REGISTRY
            = new RegistryTemplate<WestCacheKeyer>();

    static {
        KEYER_REGISTRY.register("default", new DefaultKeyer());
        KEYER_REGISTRY.register("simple", new SimpleKeyer());
        if (Envs.HAS_DIAMOND) KEYER_REGISTRY.register("packagelimit", new PackageLimitedKeyer());
    }

    public static final RegistryTemplate<WestCacheInterceptor> INTERCEPTOR_REGISTRY
            = new RegistryTemplate<WestCacheInterceptor>();

    static {
        INTERCEPTOR_REGISTRY.register("default", new ByPassInterceptor());
        if (Envs.HAS_JEDIS) INTERCEPTOR_REGISTRY.register("redis", new RedisInterceptor());
    }
}
