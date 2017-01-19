package com.github.bingoohuang.westcache.utils;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.google.common.base.Optional;
import lombok.Cleanup;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public abstract class WestCacheConnector {
    private static final ThreadLocal<Optional<?>> THREAD_LOCAL
            = new InheritableThreadLocal<Optional<?>>();

    public static boolean isConnectedAndGoon(WestCacheOption option,
                                             String cacheKey) {
        Optional<?> optional = THREAD_LOCAL.get();
        if (optional == null) return true;

        Object tag = optional.orNull();
        if (tag == ConnectTag.Option) {
            THREAD_LOCAL.set(Optional.of(option));
        } else if (tag == ConnectTag.Key) {
            THREAD_LOCAL.set(Optional.of(cacheKey));
        } else if (tag == ConnectTag.Clear) {
            option.getManager().invalidate(option, cacheKey, null);
        } else {
            option.getManager().put(option, cacheKey, new WestCacheItem(tag));
        }

        return false;
    }

    public enum ConnectTag {
        Clear,
        Option,
        Key
    }

    /**
     * Clear the cache related to callable.
     *
     * @param runnable Runnable to call cached method.
     */
    public static void clearCache(Runnable runnable) {
        connectCache(runnable, ConnectTag.Clear);
    }

    /**
     * Get the option for the cache method.
     *
     * @param runnable Runnable to call cached method.
     * @return WestCacheOption
     */
    public static WestCacheOption connectOption(Runnable runnable) {
        return connectCache(runnable, ConnectTag.Option);
    }

    /**
     * Get the key for the cache method call.
     *
     * @param runnable Runnable to call cached method.
     * @return cache key.
     */
    public static String connectKey(Runnable runnable) {
        return connectCache(runnable, ConnectTag.Key);
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
        @Cleanup val i = new QuietCloseable() {
            @Override public void close() {
                THREAD_LOCAL.remove();
            }
        };

        runnable.run();
        return (T) THREAD_LOCAL.get().orNull();
    }
}
