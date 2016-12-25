package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/25.
 */
@Service public class DemoService {
    @Setter String data;

    @WestCacheable
    public String cachedMethod() {
        return data;
    }
}
