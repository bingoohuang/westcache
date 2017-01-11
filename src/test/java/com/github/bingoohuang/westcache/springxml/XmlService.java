package com.github.bingoohuang.westcache.springxml;

import com.github.bingoohuang.westcache.WestCacheable;
import lombok.Setter;
import org.springframework.stereotype.Service;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/11.
 */
@Service
public class XmlService {
    @Setter private String data;

    @WestCacheable
    public String cachedMethod() {
        return data;
    }
}
