package com.github.bingoohuang.westcache.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.val;

import java.io.File;
import java.io.IOException;

import static com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot.USER_HOME;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondFiles {
    public static File getCacheGroupDir() {
        val configDataDir = new File(USER_HOME, ".diamond-client/config-data");
        val groupDir = new File(configDataDir, DiamondCacheManager.GROUP);
        groupDir.mkdirs();
        return groupDir;
    }

    public static void writeDiamond(File diamondFile, Object content) throws IOException {
        String json = JSON.toJSONString(content, SerializerFeature.WriteClassName);
        Files.write(json, diamondFile, Charsets.UTF_8);
    }
}
