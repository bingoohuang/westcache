package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.MallCacheable;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Helper;
import com.github.bingoohuang.westcache.utils.Redis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.github.bingoohuang.westcache.WestCacheRegistry.REGISTRY_TEMPLATE;
import static com.github.bingoohuang.westcache.outofbox.PackageLimitedKeyer.DATAID;
import static com.github.bingoohuang.westcache.outofbox.PackageLimitedKeyer.GROUP;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/30.
 */
public class BenchMarkTest {
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class BenchMallBean {
        private String name;
        private int age;
    }

    @MallCacheable
    public interface BMMallCache {
        BenchMallBean getMallBean();
    }

    static TableCacheFlusher flusher = (TableCacheFlusher) FLUSHER_REGISTRY.get("table");

    static BMMallCache mallCache = WestCacheFactory.create(BMMallCache.class);
    static BenchMallBean demo = new BenchMallBean("新几次哇一次抹黑头次", 123);

    @BeforeClass
    public static void beforeClass() {
        REGISTRY_TEMPLATE.deregister("default");
        REGISTRY_TEMPLATE.register("default",
                new DefaultWestCacheConfig() {
                    @Override public long rotateIntervalMillis() {
                        return 1000;
                    }
                });
        flusher.getDao().setup();

        MockDiamondServer.setConfigInfo(GROUP, DATAID,
                "com.github.bingoohuang.westcache");

        val cacheKey = "BenchMarkTest.BMMallCache.getMallBean";
        val bean = new WestCacheFlusherBean(cacheKey, "full",
                0, "direct", "readBy=redis");
        long lastExecuted = flusher.getLastExecuted();
        String json = FastJsons.json(demo);
        Redis.getJedis().set(Redis.PREFIX + cacheKey, json);
        flusher.getDao().addBean(bean);

        try {
            mallCache.getMallBean();
        } catch (Exception ex) {
            // ignore
        }

        // at most 15 seconds
        Helper.waitFlushRun(flusher, lastExecuted);
    }

    @Benchmark
    public void getCache() {
        BenchMallBean mallBean = mallCache.getMallBean();
        assertThat(mallBean).isEqualTo(demo);
    }

    @Test
    public void bench() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchMarkTest.class.getSimpleName())
                .forks(0)
                .warmupIterations(1)
                .measurementIterations(1)
                .threads(12)
                .build();

        new Runner(opt).run();
    }
}
