package com.github.bingoohuang.westcache;

import org.junit.Test;

public class RedisManagerCheckStartupTimeTest {
    @WestCacheable(manager = "redis", keyer = "simple", specs = "restartInvalidate=true")
    public static class RedisManagerCheckStartupTimeService {
        public String getOther() {
            return "Other" + System.currentTimeMillis();
        }
    }

    static RedisManagerCheckStartupTimeService service =
            WestCacheFactory.create(RedisManagerCheckStartupTimeService.class);

    @Test
    public void test1() {
        service.getOther();
        service.getOther();
    }
}
