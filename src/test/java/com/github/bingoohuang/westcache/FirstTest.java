package com.github.bingoohuang.westcache;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class FirstTest {
    String north = "NORTH", south = "SOUTH";

    @Test
    public void objectWrap() {
        FirstService firstService = new FirstService();
        firstService.setHomeArea(north);

        FirstService wrapService = WestCacheFactory.wrap(firstService);
        assertThat(wrapService.getHomeAreaWithCache()).isEqualTo(north);

        firstService.setHomeArea(south);
        assertThat(wrapService.getHomeAreaWithCache()).isEqualTo(north);
        assertThat(wrapService.getHomeArea()).isEqualTo(south);
    }

    @Test
    public void classWrap() {
        FirstService wrapService = WestCacheFactory.wrap(FirstService.class);
        assertThat(wrapService.getHomeAreaWithCache()).isNull();

        wrapService.setHomeArea(south);
        assertThat(wrapService.getHomeAreaWithCache()).isNull();
        assertThat(wrapService.getHomeArea()).isEqualTo(south);
    }

    public static class FirstService {
        @Getter @Setter String homeArea;

        @WestCachable
        public String getHomeAreaWithCache() {
            return homeArea;
        }
    }
}
