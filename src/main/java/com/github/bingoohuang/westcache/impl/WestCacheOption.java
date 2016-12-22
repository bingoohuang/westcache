package com.github.bingoohuang.westcache.impl;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/22.
 */
public class WestCacheOption {
    @Setter @Getter private boolean snapshot;

    public WestCacheOption(WestCacheable westCacheable) {
        snapshot = westCacheable.snapshot();
    }
}
