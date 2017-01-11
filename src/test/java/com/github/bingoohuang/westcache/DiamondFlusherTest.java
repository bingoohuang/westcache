package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import static com.github.bingoohuang.westcache.WestCacheRegistry.flusherRegistry;
import static com.github.bingoohuang.westcache.WestCacheRegistry.keyerRegistry;
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

        val keyer = keyerRegistry.get("default");
        val option = WestCacheOption.newBuilder()
                .flusher("diamond").specs("static.key=yes")
                .build();
        val cacheKey = keyer.getCacheKey(option, "getCachedContent", service);

        val flusher = flusherRegistry.get("diamond");
        flusher.flush(option, cacheKey, "");
        assertThat(service.getCachedContent()).isSameAs("111222");
    }
}
