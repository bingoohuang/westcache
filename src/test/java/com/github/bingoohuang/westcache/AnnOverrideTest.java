package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class AnnOverrideTest {
    @WestCacheable(manager = "redis", keyer = "simple")
    public static class AnnOverrideService {
        public String m1() {
            return "" + System.currentTimeMillis();
        }

        @WestCacheable(key = "fuckM2")
        public String m2() {
            return "" + System.currentTimeMillis();
        }
    }

    @Test
    public void test() {
        val service = WestCacheFactory.create(AnnOverrideService.class);
        String s1 = service.m1();
        String r1 = Redis.getJedis().get("westcache:AnnOverrideTest.AnnOverrideService.m1");
        String j1 = FastJsons.parse(r1);
        assertThat(j1).isEqualTo(s1);

        String s2 = service.m2();

        String r2 = Redis.getJedis().get("westcache:fuckM2");
        String j2 = FastJsons.parse(r2);
        assertThat(j2).isEqualTo(s2);
    }

}
