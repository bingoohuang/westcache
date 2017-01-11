package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.Envs;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/3.
 */
public class RedisInterceptorTest {
    @WestCacheable(keyer = "simple", manager = "expiring",
            interceptor = "redis", specs = "expireAfterWrite=1s")
    public static class DemoInterceptorService {
        @Setter volatile long timestamp;

        public String weixinToken() {
            return "token:" + (++timestamp);
        }
    }

    static DemoInterceptorService service =
            WestCacheFactory.create(DemoInterceptorService.class);

    @Test @SneakyThrows
    public void interceptorTest() {
        val runnable = new Runnable() {
            @SneakyThrows @Override public void run() {
                service.weixinToken();
            }
        };

        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; ++i) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }
        for (int i = 0; i < 10; ++i) {
            threads[i].join();
        }

        Envs.sleepMillis(1010L);

        for (int i = 0; i < 10; ++i) {
            threads[i] = new Thread(runnable);
            threads[i].start();
        }
        for (int i = 0; i < 10; ++i) {
            threads[i].join();
        }

        assertThat(service.weixinToken()).isEqualTo("token:2");

    }
}
