package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.manager.RedisCacheManager;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import org.junit.Test;

import java.lang.reflect.Method;

import static com.github.bingoohuang.westcache.WestCacheRegistry.keyerRegistry;
import static com.github.bingoohuang.westcache.utils.WestCacheOption.newBuilder;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class RedisManagerTest {
    @WestCacheable(manager = "redis", keyer = "simple")
    public static abstract class RedisManagerService {
        public abstract String getSomething();

        public String getOther() {
            return "Other" + System.currentTimeMillis();
        }
    }

    static RedisManagerService service = WestCacheFactory.create(RedisManagerService.class);
    static WestCacheOption option = newBuilder().manager("redis").keyer("simple")
            .method(getMethod("getOther"))
            .build();

    @SneakyThrows
    private static Method getMethod(String methodName)  {
        return RedisManagerService.class.getDeclaredMethod(methodName);
    }

    static WestCacheKeyer keyer = keyerRegistry.get("simple");
    static String getOtherCacheKey = keyer.getCacheKey(option, "getOther", service);
    static String getSomethingCacheKey = keyer.getCacheKey(option, "getSomething", service);

    @Test
    public void test() {
        Redis.getJedis().set(Redis.PREFIX + getSomethingCacheKey, "\"bingoo\"");

        String something = service.getSomething();
        assertThat(something).isEqualTo("bingoo");
    }

    @Test
    public void other() {
        String other1 = service.getOther();
        String other2 = service.getOther();
        assertThat(other1).isEqualTo(other2);

        String s = Redis.getJedis().get(Redis.PREFIX + getOtherCacheKey);
        assertThat(s).isEqualTo(FastJsons.json(other1));
    }


    @Test
    public void redisPut() {
        RedisCacheManager manager = new RedisCacheManager(Redis.PREFIX);
        manager.put(option, getOtherCacheKey, new WestCacheItem("bingoohuang"));

        WestCacheItem item = manager.get(option, getOtherCacheKey);
        assertThat(item.orNull()).isEqualTo("bingoohuang");

        manager.getWestCache().invalidate(option, getOtherCacheKey, "0");
        item = manager.get(option, getSomethingCacheKey);
        assertThat(item.orNull()).isNull();
    }
}
