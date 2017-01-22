package com.github.bingoohuang.westcache.snapshot;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Snapshots;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class FileCacheSnapshot implements WestCacheSnapshot {
    @Override @SneakyThrows
    public void saveSnapshot(WestCacheOption option,
                             String cacheKey,
                             WestCacheItem cacheValue) {
        val json = FastJsons.json(cacheValue.getObject().orNull());

        File snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        Files.write(json, snapshotFile, Charsets.UTF_8);
    }

    @Override @SneakyThrows @SuppressWarnings("unchecked")
    public WestCacheItem readSnapshot(WestCacheOption option,
                                      String cacheKey) {
        File snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return null;

        String json = Files.toString(snapshotFile, Charsets.UTF_8);
        Object object = FastJsons.parse(json, option.getMethod());
        val optional = Optional.fromNullable(object);
        return new WestCacheItem(optional, option);
    }

    @SneakyThrows @Override
    public void deleteSnapshot(WestCacheOption option, String cacheKey) {
        File snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return;

        File tempFile = File.createTempFile(cacheKey, Snapshots.EXTENSION, Snapshots.CACHE_HOME);
        snapshotFile.renameTo(tempFile);

        tempFile.delete();
    }
}
