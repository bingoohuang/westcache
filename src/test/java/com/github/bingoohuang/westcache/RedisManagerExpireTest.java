package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.Helper;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
public class RedisManagerExpireTest {
    public static volatile int calledTimes = 0;
    public static volatile int calledTimes2 = 0;

    // expireAfterWrite=[duration];
    // Durations are represented by an integer, followed by one of "d", "h", "m", or "s",
    // representing days, hours, minutes, or seconds respectively.
    @WestCacheable(manager = "redis", keyer = "simple", flusher = "table",
            specs = "expireAfterWrite=1s,redisLockFirst=true")
    public static class RedisExpireService {
        public long getTimestamp() {
            ++calledTimes;
            return System.currentTimeMillis();
        }

        @WestCacheable(specs = "expireAfterWrite=10s")
        public long getTimestamp2() {
            ++calledTimes2;
            return System.currentTimeMillis();
        }

        public String firstPush() {
            return "first";
        }
    }

    static TableCacheFlusher flusher = Helper.setupTableFlusherForTest();
    static RedisExpireService service = WestCacheFactory.create(RedisExpireService.class);

    @BeforeClass
    public static void beforeClass() {
        service.firstPush();
        Redis.getJedis().del("westcache:version:RedisManagerExpireTest.RedisExpireService.getTimestamp");
        Redis.getJedis().del("westcache:version:RedisManagerExpireTest.RedisExpireService.getTimestamp2");
    }

    @AfterClass
    public static void afterClass() {
        flusher.cancelRotateChecker();
    }

    public static final int COUNT = 20;

    @Test
    public void test() {
        val prefix = "RedisManagerExpireTest.RedisExpireService.getTimestamp";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "none", null);
        Helper.addConfigBean(flusher, bean);

        runThreads(1);
        assertThat(calledTimes).isEqualTo(1);

        Envs.sleepMillis(1000L);
        runThreads(1);
        assertThat(calledTimes).isEqualTo(2);
    }

    @Test
    public void test2() {
        val prefix = "RedisManagerExpireTest.RedisExpireService.getTimestamp2";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "none", null);
        Helper.addConfigBean(flusher, bean);
        service.getTimestamp2();
        assertThat(calledTimes2).isEqualTo(1);

        upgradeVersion(prefix);
        assertThat(calledTimes2).isEqualTo(2);
    }

    private void upgradeVersion(String prefix) {
        Helper.upgradeVersion(prefix, flusher);

        runThreads(2);
    }

    @SneakyThrows
    private void runThreads(final int methodIndex) {
        val cdl = new CountDownLatch(COUNT);
        Runnable runnable = new Runnable() {
            @Override @SneakyThrows public void run() {
                cdl.await();
                if (methodIndex == 1) service.getTimestamp();
                else if (methodIndex == 2) service.getTimestamp2();
            }
        };

        Thread[] threads = new Thread[COUNT];
        for (int i = 0; i < threads.length; ++i) {
            threads[i] = new Thread(runnable);
            threads[i].start();
            cdl.countDown();
        }
        for (int i = 0; i < threads.length; ++i) {
            threads[i].join();
        }
    }

}
