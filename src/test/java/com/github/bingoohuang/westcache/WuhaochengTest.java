package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import lombok.Data;
import org.junit.Test;

public class WuhaochengTest {
    @Data
    static class CacheClass {
        private String value = "";

        @WestCacheable(manager = "expiring", specs = "static.key=yes,expireAfterWrite=10m")
        public String cache(String key) {
            System.err.println("create cache..." + getValue());
            return "@" + key + ":" + getValue();
        }
    }

    static CacheClass cache = WestCacheFactory.create(new CacheClass());

    @Test
    public void testCache() {
        cache.setValue("abc");
        System.out.println();
        System.err.println("should create...");
        System.out.println(cache.cache("111"));
        System.err.println("should cached...");
        System.out.println(cache.cache("111"));
        cache.setValue("def");
        System.err.println("should cached...");
        System.out.println(cache.cache("111"));
        System.err.println("should cached...");
        System.out.println(cache.cache("111"));

        WestCacheConnector.clearCache(() -> cache.cache("111"));
        System.err.println("should create...");
        System.out.println(cache.cache("111"));
        System.err.println("should cached...");
        System.out.println(cache.cache("111"));
    }
}
