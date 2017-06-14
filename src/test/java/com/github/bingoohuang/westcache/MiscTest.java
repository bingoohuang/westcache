package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.ByPassCacheFlusher;
import lombok.val;
import org.junit.Test;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
public class MiscTest {
    @Test
    public void byPassCacheFlusherFlush() {
        val flusher = new ByPassCacheFlusher();
        flusher.flush(null, null, null);

        val config = new DefaultWestCacheConfig();
        config.rotateIntervalMillis();
    }
}
