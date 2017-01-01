package com.github.bingoohuang.westcache.keyer;

import com.github.bingoohuang.westcache.base.WestCacheKeyer;
import com.github.bingoohuang.westcache.utils.Keys;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.val;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class DefaultKeyer extends WestCacheKeyer {
    @Override
    public String getCacheKey(WestCacheOption option,
                              String methodName,
                              Object bean,
                              Object... args) {
        if (option.getKey().length() > 0) return option.getKey();

        val mainPart = Keys.createKeyMainPart(methodName, bean, false);

        val staticKey = option.getSpecs().get("static.key");
        val hashCode = option.getSnapshot() != null || "yes".equals(staticKey)
                ? "" : "." + bean.hashCode();

        return mainPart + hashCode + Keys.joinArgs(args);
    }
}
