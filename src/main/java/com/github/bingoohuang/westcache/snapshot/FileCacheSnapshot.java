package com.github.bingoohuang.westcache.snapshot;

import com.alibaba.fastjson.JSON;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;

import static com.alibaba.fastjson.serializer.SerializerFeature.WriteClassName;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class FileCacheSnapshot implements WestCacheSnapshot {
    @Override @SneakyThrows
    public void saveSnapshot(WestCacheOption option, String cacheKey, Object cacheValue) {
        val json = JSON.toJSONString(cacheValue, WriteClassName);

        File snapshotFile = getSnapshotFile(cacheKey);
        Files.write(json, snapshotFile, Charsets.UTF_8);
    }

    @Override @SneakyThrows @SuppressWarnings("unchecked")
    public Optional<Object> readSnapshot(WestCacheOption option, String cacheKey) {
        File snapshotFile = getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return null;

        String json = Files.toString(snapshotFile, Charsets.UTF_8);
        Object object = JSON.parse(json);
        return Optional.fromNullable(object);
    }

    @Override public void deleteSnapshot(String cacheKey) {
        File snapshotFile = getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return;

        snapshotFile.delete();
    }

    public static String USER_HOME = System.getProperty("user.home");
    public static String EXTENSION = ".westcache";

    public static File getSnapshotFile(String cacheKey) {
        File westCacheHome = tryCreateWestCacheHome();
        return new File(westCacheHome, cacheKey + EXTENSION);
    }

    private static File tryCreateWestCacheHome() {
        val westCacheHome = new File(USER_HOME, EXTENSION);
        westCacheHome.mkdirs();
        return westCacheHome;
    }
}
