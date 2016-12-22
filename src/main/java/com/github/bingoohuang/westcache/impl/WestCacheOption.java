package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheFlusherManager;
import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.base.WestCacheable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
@NoArgsConstructor @AllArgsConstructor
public class WestCacheOption {
    @Getter private boolean snapshot;
    @Getter private WestCacheFlusher westCacheFlusher;

    public WestCacheOption(WestCacheable westCacheable) {
        snapshot = westCacheable.snapshot();
        westCacheFlusher = WestCacheFlusherManager.get(westCacheable.flusher());
    }
}
