package com.github.bingoohuang.westcache.peng;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Helper;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.JedisCommands;

import static com.github.bingoohuang.westcache.outofbox.PackageLimitedKeyer.DATAID;
import static com.github.bingoohuang.westcache.outofbox.PackageLimitedKeyer.GROUP;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PengConfig.class})
public class BasicDataCacheTest {
    private static TableCacheFlusher flusher;
    @Autowired BasicDataCache cache;
    @Autowired @Qualifier("singleRedis") JedisCommands jedis;


    @BeforeClass
    public static void beforeClass() {
        flusher = Helper.setupTableFlusherForTest();
    }

    @AfterClass
    public static void afterClass() {
        flusher.cancelRotateChecker();
    }

    @Test
    public void test() {
        MockDiamondServer.setConfigInfo(GROUP, DATAID,
                "com.github.bingoohuang.westcache.peng");
        cache.firstPush();

        val prefix = "mall.commonParam";
        String redisAbc = Redis.PREFIX + prefix + "_abc";
        jedis.del(redisAbc);
        String redisEfg = Redis.PREFIX + prefix + "_efg";
        jedis.del(redisEfg);

        val bean = new WestCacheFlusherBean(prefix, "prefix",
                0, "none", null);
        Helper.addConfigBean(flusher, bean);

        val abc = cache.qryCommParam("abc");
        val efg = cache.qryCommParam("efg");

        val abcBean = new CommparaBean("abc", "code", "name");
        assertThat(abc).containsExactly(abcBean);
        val efgBean = new CommparaBean("efg", "code", "name");
        assertThat(efg).containsExactly(efgBean);

        assertThat(jedis.get(redisAbc)).isEqualTo(FastJsons.json(abc));
        assertThat(jedis.get(redisEfg)).isEqualTo(FastJsons.json(efg));
        assertThat(jedis.ttl(redisAbc)).isAtMost(10 * 60L);
        assertThat(jedis.ttl(redisEfg)).isAtMost(10 * 60L);
    }
}
