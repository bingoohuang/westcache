package com.github.bingoohuang.westcache.snapshot;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.github.bingoohuang.westcache.base.WestCacheSnapshot;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Redis;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import com.google.common.base.Optional;
import lombok.AllArgsConstructor;
import lombok.val;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@AllArgsConstructor
public class RedisCacheSnapshot implements WestCacheSnapshot {
    final String prefix;

    public RedisCacheSnapshot() {
        this(Redis.PREFIX);
    }

    @Override
    public void saveSnapshot(WestCacheOption option,
                             String cacheKey,
                             WestCacheItem cacheValue) {
        val json = FastJsons.json(cacheValue.getObject().orNull());
        Redis.getRedis(option).set(prefix + cacheKey, json);
    }

    @Override
    public WestCacheItem readSnapshot(WestCacheOption option,
                                      String cacheKey) {
        val json = Redis.getRedis(option).get(prefix + cacheKey);
        if (json == null) return null;

        val object = FastJsons.parse(json, option.getMethod(), true);
        val optional = Optional.fromNullable(object);
        return new WestCacheItem(optional, option);
    }

    @Override
    public void deleteSnapshot(WestCacheOption option, String cacheKey) {
        Redis.getRedis(option).del(prefix + cacheKey);
    }
}
