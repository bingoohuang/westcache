package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/5.
 */
public class CustomAnnOverrideTest {
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(snapshot = "file", key = "abc")
    public @interface Ann1 {
        String key() default "";
    }

    public static class AnnService {
        @Ann1
        public String m1() {
            return "" + System.currentTimeMillis();
        }

        @Ann1(key = "mm22")
        public String m2() {
            return "" + System.currentTimeMillis();
        }
    }

    @Test @SneakyThrows
    public void test() {
        val service = WestCacheFactory.create(AnnService.class);
        String m2 = service.m2();
        String m3 = service.m2();

        assertThat(m2).isSameAs(m3);

        val opt = WestCacheOption.builder().key("mm22").snapshot("file").build();
        WestCacheItem item = opt.getManager().get(opt, "mm22");
        assertThat(item.orNull()).isSameAs(m3);
    }
}
