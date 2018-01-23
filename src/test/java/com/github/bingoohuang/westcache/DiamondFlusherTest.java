package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.github.bingoohuang.westcache.WestCacheRegistry.KEYER_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
public class DiamondFlusherTest {
    public static class ServiceDemoForDiamond {
        @Setter private String content;

        @WestCacheable(flusher = "diamond", specs = "static.key=yes")
        public String getCachedContent() {
            return content;
        }
    }

    @Test @SneakyThrows
    public void diamondFlush() {
        val service = WestCacheFactory.create(ServiceDemoForDiamond.class);
        service.setContent("XXXyyy");

        assertThat(service.getCachedContent()).isSameAs("XXXyyy");

        service.setContent("111222");
        assertThat(service.getCachedContent()).isSameAs("XXXyyy");

        val keyer = KEYER_REGISTRY.get("default");
        val option = WestCacheOption.builder()
                .flusher("diamond").specs("static.key=yes")
                .build();
        val cacheKey = keyer.getCacheKey(option, "getCachedContent", service);

        val flusher = FLUSHER_REGISTRY.get("diamond");
        flusher.flush(option, cacheKey, "");
        assertThat(service.getCachedContent()).isSameAs("111222");
    }
}
