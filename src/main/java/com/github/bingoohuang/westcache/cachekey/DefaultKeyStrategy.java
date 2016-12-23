package com.github.bingoohuang.westcache.cachekey;

import com.github.bingoohuang.westcache.WestCacheOptions;
import com.github.bingoohuang.westcache.base.WestCacheKeyStrategy;

import static com.github.bingoohuang.westcache.utils.CglibUtils.getSuperClassName;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultKeyStrategy implements WestCacheKeyStrategy {
    @Override
    public String getCacheKey(WestCacheOptions option, String methodName, Object bean, Object... args) {
        if (option.getKey().length() > 0) return option.getKey();

        String baseCacheKey = getSuperClassName(bean) + "." + methodName;
        return option.getSnapshot() != null
                ? baseCacheKey
                : baseCacheKey + "." + bean.hashCode();
    }
}
