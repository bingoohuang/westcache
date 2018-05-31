package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.google.common.base.Optional;
import lombok.Cleanup;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public abstract class WestCacheConnector {
    private static final ThreadLocal<Optional<?>> THREAD_LOCAL = new InheritableThreadLocal<>();

    public static boolean isConnectedAndGoon(WestCacheOption option, String cacheKey) {
        val optional = THREAD_LOCAL.get();
        if (optional == null) return false;

        val tag = optional.orNull();
        if (tag == ConnectTag.OPTION) {
            THREAD_LOCAL.set(Optional.of(option));
        } else if (tag == ConnectTag.KEY) {
            THREAD_LOCAL.set(Optional.of(cacheKey));
        } else if (tag == ConnectTag.CLEAR) {
            option.getManager().invalidate(option, cacheKey, null);
        } else {
            option.getManager().put(option, cacheKey,
                    new WestCacheItem(Optional.fromNullable(tag), option));
        }

        return true;
    }

    public enum ConnectTag {
        CLEAR,
        OPTION,
        KEY
    }

    /**
     * CLEAR the cache related to callable.
     *
     * @param runnable Runnable to call cached method.
     */
    public static void clearCache(Runnable runnable) {
        connectCache(runnable, ConnectTag.CLEAR);
    }

    /**
     * Get the option for the cache method.
     *
     * @param runnable Runnable to call cached method.
     * @return WestCacheOption
     */
    public static WestCacheOption connectOption(Runnable runnable) {
        return connectCache(runnable, ConnectTag.OPTION);
    }

    /**
     * Get the key for the cache method call.
     *
     * @param runnable Runnable to call cached method.
     * @return cache key.
     */
    public static String connectKey(Runnable runnable) {
        return connectCache(runnable, ConnectTag.KEY);
    }


    /**
     * Connect the cache with the new cached value.
     *
     * @param runnable    Runnable to call cached method.
     * @param cachedValue new cached value
     * @param <T>         cached value type
     * @return cached value.
     */
    public static <T> T connectCache(Runnable runnable, Object cachedValue) {
        THREAD_LOCAL.set(Optional.fromNullable(cachedValue));
        @Cleanup QuietCloseable i = () -> THREAD_LOCAL.remove();

        runnable.run();
        return (T) THREAD_LOCAL.get().orNull();
    }
}
