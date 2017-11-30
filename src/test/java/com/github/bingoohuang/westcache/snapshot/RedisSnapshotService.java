package com.github.bingoohuang.westcache.snapshot;

import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.utils.Envs;
import org.apache.commons.lang3.RandomStringUtils;

public class RedisSnapshotService {
    @WestCacheable(keyer = "simple", snapshot = "redis", specs = "expireAfterWrite=5m")
    public String cacheGet(String id) {
//        Envs.sleepMillis(1200);
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
