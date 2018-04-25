package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class CacheApiTest {
    String north = "NORTH", south = "SOUTH";
    @Getter @Setter String homeArea;

    public String getHomeAreaWithCache() {
        return homeArea;
    }

    @Test
    public void apiBasic() {
        setHomeArea(north);
        String cacheKey = "api.cache.key";
        val option = WestCacheOption.builder().build();
        val manager = option.getManager();
        WestCacheItem cache = manager.get(option, cacheKey,
                new Callable<WestCacheItem>() {
                    @Override public WestCacheItem call() {
                        Object homeAreaWithCache = getHomeAreaWithCache();
                        val optional = Optional.fromNullable(homeAreaWithCache);
                        return new WestCacheItem(optional, option);
                    }
                });
        assertThat(cache.getObject().orNull()).isEqualTo(north);

        setHomeArea(south);
        cache = manager.get(option, cacheKey);
        assertThat(cache.orNull()).isEqualTo(north);
        assertThat(getHomeArea()).isEqualTo(south);
    }
}
