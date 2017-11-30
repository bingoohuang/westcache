package com.github.bingoohuang.westcache.snapshot;

import com.github.bingoohuang.westcache.WestCacheFactory;
import lombok.val;
import org.junit.Test;

public class RedisSnapshotTest {
    @Test
    public void test() {
        val redisSnapshotService = WestCacheFactory.create(RedisSnapshotService.class);
        String abc = redisSnapshotService.cacheGet("abc");
        System.out.println(abc);

        String efg = redisSnapshotService.cacheGet("efg");
        System.out.println(efg);
    }
}
