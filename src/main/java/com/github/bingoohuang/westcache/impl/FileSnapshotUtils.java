package com.github.bingoohuang.westcache.impl;

import com.alibaba.fastjson.JSON;
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
public class FileSnapshotUtils {
    public static File getSnapshotFile(String cacheKey) {
        val userHome = System.getProperty("user.home");
        val westCacheHome = new File(userHome, ".westcache");
        westCacheHome.mkdir();

        return new File(westCacheHome, cacheKey + ".westcache");
    }

    @SneakyThrows
    public static void saveSnapshot(String cacheKey, Object result) {
        val json = JSON.toJSONString(result, WriteClassName);

        File snapshotFile = getSnapshotFile(cacheKey);
        Files.write(json, snapshotFile, Charsets.UTF_8);
    }

    @SneakyThrows
    public static Optional<Object> readSnapShot(String cacheKey) {
        File snapshotFile = getSnapshotFile(cacheKey);
        if (!snapshotFile.exists() || !snapshotFile.isFile()) return null;

        String json = Files.toString(snapshotFile, Charsets.UTF_8);
        Object object = JSON.parse(json);
        return Optional.of(object);
    }
}
