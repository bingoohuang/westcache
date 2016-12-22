package com.github.bingoohuang.westcache.base;

import com.google.common.base.Optional;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public interface WestCacheSnapshot {
    void saveSnapshot(String cacheKey, Object cacheValue);

    <T> Optional<T> readSnapshot(String cacheKey);
}
