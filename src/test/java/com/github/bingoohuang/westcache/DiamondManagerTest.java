package com.github.bingoohuang.westcache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.bingoohuang.westcache.base.WestCacheable;
import com.github.bingoohuang.westcache.manager.DiamondCacheManager;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import java.io.File;

import static com.github.bingoohuang.westcache.WestCacheOptions.newBuilder;
import static com.github.bingoohuang.westcache.snapshot.FileCacheSnapshot.USER_HOME;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondManagerTest {
    public interface DiamondService {
        @WestCacheable(manager = "diamond", specs = "static.key=yes")
        String getBigData();
    }


    @Test @SneakyThrows
    public void test() {
        val service = WestCacheFactory.create(DiamondService.class);

        val configDataDir = new File(USER_HOME, ".diamond-client/config-data");
        val groupDir = new File(configDataDir, DiamondCacheManager.GROUP);
        groupDir.mkdirs();

        val keyStrategy = WestCacheRegistry.getKeyStrategy("default");
        val option = newBuilder().manager("diamond").specs("static.key=yes").build();
        val cacheKey = keyStrategy.getCacheKey(option, "getBigData", service);
        val diamondFile = new File(groupDir, cacheKey + ".diamond");
        String content = "Here is Bingoo!" + System.currentTimeMillis();
        String json = JSON.toJSONString(content, SerializerFeature.WriteClassName);
        Files.write(json, diamondFile, Charsets.UTF_8);

        assertThat(service.getBigData()).isEqualTo(content);
    }
}
