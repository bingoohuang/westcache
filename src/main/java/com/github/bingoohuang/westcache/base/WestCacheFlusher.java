package com.github.bingoohuang.westcache.base;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheFlusher {
    void register(String cacheKey, WestCache<String, Object> cache);

    void flush(String cacheKey);
}
