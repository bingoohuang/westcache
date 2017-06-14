package com.github.bingoohuang.westcache.flusher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Data @AllArgsConstructor @NoArgsConstructor
public class WestCacheFlusherBean {
    private String cacheKey;
    private String keyMatch;
    private int valueVersion;
    private String valueType;
    private String specs;
}
