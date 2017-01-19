package com.github.bingoohuang.westcache.utils;

import com.google.common.base.Optional;
import lombok.Cleanup;
import lombok.val;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public abstract class WestCacheConnector {
    private static final ThreadLocal<Optional<Object>> THREADLOCAL
            = new InheritableThreadLocal<Optional<Object>>();
    private static final Object CLEARTAG = new Object();
    private static final Object OPTIONTAG = new Object();

    public static boolean isThreadLocalEmpty() {
        return THREADLOCAL.get() == null;
    }

    public static boolean isThreadLocalOptionTag() {
        Optional<Object> optional = THREADLOCAL.get();
        return optional != null && optional.orNull() == OPTIONTAG;
    }

    public static boolean isThreadLocalClearTag() {
        Optional<Object> optional = THREADLOCAL.get();
        return optional != null && optional.orNull() == CLEARTAG;
    }

    public static Optional<Object> getThreadLocal() {
        return THREADLOCAL.get();
    }

    public static void setThreadLocal(Object object) {
        THREADLOCAL.set(Optional.fromNullable(object));
    }

    /**
     * Clear the cache related to callable.
     *
     * @param runnable Runnable to call cached method.
     */
    public static void clearCache(Runnable runnable) {
        connectCache(runnable, CLEARTAG);
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
        THREADLOCAL.set(Optional.fromNullable(cachedValue));
        @Cleanup val i = new QuietCloseable() {
            @Override public void close() {
                THREADLOCAL.remove();
            }
        };

        runnable.run();
        return (T) cachedValue;
    }

    /**
     * Get the option for the cache method.
     *
     * @param runnable Runnable to call cached method.
     * @return WestCacheOption
     */
    public static WestCacheOption getWestCacheOption(Runnable runnable) {
        THREADLOCAL.set(Optional.fromNullable(OPTIONTAG));
        @Cleanup val i = new QuietCloseable() {
            @Override public void close() {
                THREADLOCAL.remove();
            }
        };

        runnable.run();
        return (WestCacheOption) THREADLOCAL.get().orNull();
    }
}
