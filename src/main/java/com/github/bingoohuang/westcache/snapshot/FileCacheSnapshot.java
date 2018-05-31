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

import static com.github.bingoohuang.westcache.utils.Snapshots.CACHE_HOME;
import static com.github.bingoohuang.westcache.utils.Snapshots.EXTENSION;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class FileCacheSnapshot implements WestCacheSnapshot {
    @Override @SneakyThrows
    public void saveSnapshot(WestCacheOption option,
                             String cacheKey,
                             WestCacheItem cacheValue) {
        val json = FastJsons.json(cacheValue.getObject().orNull());

        val snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        Files.write(json, snapshotFile, Charsets.UTF_8);
    }

    @Override @SneakyThrows
    public WestCacheItem readSnapshot(WestCacheOption option,
                                      String cacheKey) {
        val snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return null;

        val json = Files.asCharSource(snapshotFile, Charsets.UTF_8).read();
        val object = FastJsons.parse(json, option.getMethod(), true);
        val optional = Optional.fromNullable(object);
        return new WestCacheItem(optional, option);
    }

    @Override @SneakyThrows
    public void deleteSnapshot(WestCacheOption option, String cacheKey) {
        val snapshotFile = Snapshots.getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return;

        val tempFile = File.createTempFile(cacheKey, EXTENSION, CACHE_HOME);
        snapshotFile.renameTo(tempFile);

        tempFile.delete();
    }
}
