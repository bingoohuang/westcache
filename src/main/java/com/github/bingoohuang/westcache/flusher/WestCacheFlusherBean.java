package com.github.bingoohuang.westcache.flusher;

import lombok.*;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Data @AllArgsConstructor @NoArgsConstructor public class WestCacheFlusherBean {
    @Getter @Setter private String cacheKey, keyMatch;
    @Getter @Setter private int valueVersion;
    @Getter @Setter private String valueType;
}
