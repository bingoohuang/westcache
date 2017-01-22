package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.manager.RedisCacheManager;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.val;
import org.junit.Test;

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

    static String getOtherCacheKey = WestCacheConnector.connectKey(new Runnable() {
        @Override public void run() {
            service.getOther();
        }
    });
    static String getSomethingCacheKey = WestCacheConnector.connectKey(new Runnable() {
        @Override public void run() {
            service.getSomething();
        }
    });

    static WestCacheOption option = WestCacheConnector.connectOption(new Runnable() {
        @Override public void run() {
            service.getOther();
        }
    });

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
        val manager = new RedisCacheManager(Redis.PREFIX);
        manager.put(option, getOtherCacheKey, new WestCacheItem(Optional.of("bingoohuang"), option));

        WestCacheItem item = manager.get(option, getOtherCacheKey);
        assertThat(item.orNull()).isEqualTo("bingoohuang");

        manager.getWestCache().invalidate(option, getOtherCacheKey, null);
        item = manager.get(option, getSomethingCacheKey);
        assertThat(item.orNull()).isNull();
    }
}
