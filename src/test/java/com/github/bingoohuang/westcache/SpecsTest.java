package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.keyer.DefaultKeyer;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.Specs;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

import static com.github.bingoohuang.westcache.WestCacheRegistry.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/24.
 */
public class SpecsTest {
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", config = "specsTestConfig",
            keyer = "prefix", specs = "key.prefix=demo.")
    public @interface DemoCacheMe {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomizedCacheKey {
        String value();
    }

    @BeforeClass
    public static void beforeClass() {
        KEYER_REGISTRY.register("prefix", new DefaultKeyer() {
            @Override
            public String getCacheKey(WestCacheOption option,
                                      Method method,
                                      Object bean,
                                      Object... args) {
                val keyPrefix = option.getSpecs().get("key.prefix");
                val ann = method.getAnnotation(CustomizedCacheKey.class);

                String cacheKey = keyPrefix + (ann != null ? ann.value() : method.getName());
                return cacheKey;
            }
        });
        REGISTRY_TEMPLATE.register("specsTestConfig",
                new DefaultWestCacheConfig() {
                    @Override
                    public long timeoutMillisToSnapshot() {
                        return 100L;
                    }
                });
    }

    @AfterClass
    public static void afterClass() {
        KEYER_REGISTRY.deregister("prefix");
        REGISTRY_TEMPLATE.deregister("specsTestConfig");
    }

    public static class MyService {
        @Getter @Setter private List<String> provinces;
        @Setter long sleepMillis;
        @Getter @Setter private volatile boolean cacheMethodExecuted = false;

        @DemoCacheMe @CustomizedCacheKey("provinces")
        public List<String> getProvincesCache() {
            Envs.sleepMillis(sleepMillis);
            setCacheMethodExecuted(true);
            return provinces;
        }
    }

    @Test @SneakyThrows
    public void testKey() {
        val myService = WestCacheFactory.create(MyService.class);

        String cacheKey = "demo.provinces";
        List<String> provinces = Lists.newArrayList("江苏省", "浙江省");
        val fileSnapshot = SNAPSHOT_REGISTRY.get("file");

        fileSnapshot.saveSnapshot(null, cacheKey, new WestCacheItem(Optional.fromNullable(provinces), null));

        List<String> otherProvinces = Lists.newArrayList("广东省", "广西省");
        myService.setProvinces(otherProvinces);
        myService.setSleepMillis(300L);

        List<String> cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(provinces);

        do {
            Envs.sleepMillis(50L);
        } while (!myService.isCacheMethodExecuted());

        cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(otherProvinces);
    }


    @Test
    public void testParse() {
        Map<String, String> map = Specs.parseSpecs("a=1,b=2");
        assertThat(map).isEqualTo(ImmutableMap.of("a", "1", "b", "2"));
    }
}
