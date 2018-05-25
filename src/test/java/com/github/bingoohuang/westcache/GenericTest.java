package com.github.bingoohuang.westcache;

import lombok.*;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class GenericTest {
    @Data @AllArgsConstructor @NoArgsConstructor
    public static class GenericBean {
        private String name;
    }

    public static class GenericService {
        @Getter @Setter Object genericBean;

        @WestCacheable(manager = "redis", keyer = "simple")
        public <T> T getGenericBean() {
            return (T) genericBean;
        }
    }

    @Test
    public void test() {
        val service = WestCacheFactory.create(GenericService.class);
        GenericBean bingoo = new GenericBean("bingoo");
        service.setGenericBean(bingoo);
        Object bean = service.getGenericBean();
        assertThat(bean).isEqualTo(bingoo);
    }
}
