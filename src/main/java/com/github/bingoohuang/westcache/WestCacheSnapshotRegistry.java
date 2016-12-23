package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.experimental.UtilityClass;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
@UtilityClass
public class WestCacheSnapshotRegistry {
    Cache<String, WestCacheSnapshot> registry = CacheBuilder.newBuilder().build();

    static {
        registerSnapshot("file", new FileCacheSnapshot());
    }

    public void registerSnapshot(String snapshotName, WestCacheSnapshot snapshot) {
        WestCacheSnapshot cacheSnapshot = registry.getIfPresent(snapshotName);
        if (cacheSnapshot != null) throw new RuntimeException(
                "snapshot name " + snapshotName + " already exists");

        registry.put(snapshotName, snapshot);
    }

    public void deregisterFlusher(String snapshotName) {
        registry.invalidate(snapshotName);
    }

    public WestCacheSnapshot get(String snapshotName) {
        return registry.getIfPresent(snapshotName);
    }
}
