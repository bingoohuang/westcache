package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.cglib.Cglibs;
import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.ByPassCacheFlusher;
import com.github.bingoohuang.westcache.utils.*;
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

        new Anns(){};
        new Durations(){};
        new Envs(){};
        new FastJsons(){};
        new Keys(){};
        new Redis(){};
        new Snapshots(){};
        new Specs(){};
        new Cglibs(){};
        new WestCacheFactory(){};
        new WestCacheRegistry(){};
        new Guavas(){};
        new WestCacheConnector() {};
        new ExpireAfterWrites(){};
    }
}
