package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cachekey.DefaultKeyStrategy;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.utils.WestCacheOptions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/24.
 */
public class SpecsTest {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", config = "specsTestConfig",
            keyStrategy = "prefix", specs = "key.prefix=demo.")
    public @interface DemoCacheMe {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomizedCacheKey {
        String value();
    }

    @BeforeClass
    public static void beforeClass() {
        registerKeyStrategy("prefix", new DefaultKeyStrategy() {
            @Override
            public String getCacheKey(WestCacheOptions option,
                                      Method method,
                                      Object bean,
                                      Object... args) {
                val mapSplitter = Splitter.on(';').withKeyValueSeparator('=');
                val map = mapSplitter.split(option.getSpecs());
                val keyPrefix = map.get("key.prefix");
                val ann = method.getAnnotation(CustomizedCacheKey.class);

                String cacheKey = keyPrefix + (ann != null ? ann.value() : method.getName());
                return cacheKey;
            }
        });
        registerConfig("specsTestConfig",
                new DefaultWestCacheConfig() {
                    @Override
                    public long timeoutMillisToSnapshot() {
                        return 100L;
                    }
                });
    }

    @AfterClass
    public static void afterClass() {
        deregisterKeyStrategy("prefix");
        deregisterConfig("specsTestConfig");
    }

    public static class MyService {
        @Getter @Setter private List<String> provinces;
        @Setter long sleepMillis;
        @Getter @Setter private volatile boolean cacheMethodExecuted = false;

        @DemoCacheMe @CustomizedCacheKey("provinces") @SneakyThrows
        public List<String> getProvincesCache() {
            Thread.sleep(sleepMillis);
            setCacheMethodExecuted(true);
            return provinces;
        }
    }

    @Test @SneakyThrows
    public void testKey() {
        val myService = WestCacheFactory.create(MyService.class);

        String cacheKey = "demo.provinces";
        List<String> provinces = Lists.newArrayList("江苏省", "浙江省");
        val fileSnapshot = getSnapshot("file");
        fileSnapshot.saveSnapshot(cacheKey, provinces);

        List<String> otherProvinces = Lists.newArrayList("广东省", "广西省");
        myService.setProvinces(otherProvinces);
        myService.setSleepMillis(300L);

        List<String> cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(provinces);

        do {
            Thread.sleep(50L);
        } while (!myService.isCacheMethodExecuted());

        cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(otherProvinces);
    }
}
