package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheable;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/21.
 */
public class FirstTest {
    String north = "NORTH", south = "SOUTH";

    @Test
    public void cacheBasic() {
        val firstService = new FirstService();
        firstService.setHomeArea(north);

        val wrapService = WestCacheFactory.create(firstService);
        assertThat(wrapService.getHomeAreaWithCache()).isEqualTo(north);

        wrapService.setHomeArea(south);
        assertThat(wrapService.getHomeAreaWithCache()).isEqualTo(north);
        assertThat(wrapService.getHomeArea()).isEqualTo(south);
    }

    @Test
    public void cacheNull() {
        val wrapService = WestCacheFactory.create(FirstService.class);
        assertThat(wrapService.getHomeAreaWithCache()).isNull();

        wrapService.setHomeArea(south);
        assertThat(wrapService.getHomeAreaWithCache()).isNull();
        assertThat(wrapService.getHomeArea()).isEqualTo(south);
    }

    public static class FirstService {
        @Getter @Setter String homeArea;

        @WestCacheable
        public String getHomeAreaWithCache() {
            return homeArea;
        }
    }
}
