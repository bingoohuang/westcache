package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.base.WestCache;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.n3r.diamond.client.DiamondManager;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@Slf4j
public class DiamondCacheFlusher extends SimpleCacheFlusher {
    public static final String GROUP = "west.cache.flushers";

    @Override
    public boolean register(final WestCacheOption option,
                            final String cacheKey,
                            WestCache cache) {
        val firstRegistered = super.register(option, cacheKey, cache);
        if (!firstRegistered) return false;

        val diamondManager = new DiamondManager(GROUP, cacheKey);
        diamondManager.addDiamondListener(diamondStone -> flush(option, cacheKey, ""));

        log.debug("add diamond listener for group={}, dataid={}", GROUP, cacheKey);

        return firstRegistered;
    }
}
