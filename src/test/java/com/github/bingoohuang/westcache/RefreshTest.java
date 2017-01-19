package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class RefreshTest {
    String north = "NORTH", south = "SOUTH";

    public static class FlushBean {
        @Getter @Setter String homeArea;

        @WestCacheable(flusher = "simple")
        public String getHomeAreaWithCache() {
            return homeArea;
        }
    }

    @Test
    public void flush() {
        val bean = WestCacheFactory.create(FlushBean.class);

        bean.setHomeArea(north);
        String cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        bean.setHomeArea(south);
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(north);

        val option = WestCacheConnector.connectOption(new Runnable() {
            @Override public void run() {
                bean.getHomeAreaWithCache();
            }
        });
        WestCacheRegistry.flush(option, bean, "getHomeAreaWithCache");
        cached = bean.getHomeAreaWithCache();
        assertThat(cached).isEqualTo(south);
    }
}
