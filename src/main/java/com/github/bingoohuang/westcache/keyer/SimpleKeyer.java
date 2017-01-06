package com.github.bingoohuang.westcache.keyer;

import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.utils.Keys;
import com.github.bingoohuang.westcache.utils.WestCacheOption;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class SimpleKeyer extends WestCacheKeyer {
    @Override
    public String getCacheKey(WestCacheOption option,
                              String methodName,
                              Object bean,
                              Object... args) {
        String mainPart = option.getKey().length() > 0
                ? option.getKey()
                : Keys.createKeyMainPart(methodName, bean, true);

        return mainPart + Keys.joinArgs(args);
    }
}
