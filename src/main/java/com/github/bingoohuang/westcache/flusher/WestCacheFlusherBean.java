package com.github.bingoohuang.westcache.flusher;

import lombok.*;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Data @AllArgsConstructor @NoArgsConstructor public class WestCacheFlusherBean {
   private String cacheKey, keyMatch;
   private int valueVersion;
   private String valueType;
   private String specs;
}
