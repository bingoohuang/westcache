package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
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


    @Test
    public void test() {
        Redis.getJedis().set(Redis.PREFIX + "RedisManagerTest" +
                ".RedisManagerService.getSomething", "\"bingoo\"");

        String something = service.getSomething();
        assertThat(something).isEqualTo("bingoo");
    }

    @Test
    public void other() {
        String other1 = service.getOther();
        String other2 = service.getOther();
        assertThat(other1).isEqualTo(other2);

        String s = Redis.getJedis().get(Redis.PREFIX + "RedisManagerTest" +
                ".RedisManagerService.getOther");
        assertThat(s).isEqualTo(FastJsons.json(other1));
    }
}
