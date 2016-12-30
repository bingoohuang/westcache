package com.github.bingoohuang.westcache.flusher;

import com.google.common.base.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
@Data @AllArgsConstructor @NoArgsConstructor public class WestCacheFlusherBean {
    private String cacheKey, keyMatch;
    private int valueVersion;
    private String valueType;
    private String specs;

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        WestCacheFlusherBean that = (WestCacheFlusherBean) o;

        return Objects.equal(cacheKey, that.cacheKey)
                && Objects.equal(valueVersion, that.valueVersion);
    }

    @Override public int hashCode() {
        return Objects.hashCode(cacheKey, valueVersion);
    }
}
