package com.github.bingoohuang.westcache;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.Test;

import java.util.List;

import static com.github.bingoohuang.westcache.utils.Snapshots.getSnapshotFile;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/23.
 */
public class CacheKeyTest {
    public static class MyService {
        @Getter @Setter private List<String> provinces;

        @WestCacheable(snapshot = "file", key = "provinces")
        public List<String> getProvincesCache() {
            return provinces;
        }
    }

    @Test
    public void testKey() {
        val myService = WestCacheFactory.create(MyService.class);
        List<String> provinces = Lists.newArrayList("江苏省", "浙江省");
        myService.setProvinces(provinces);

        List<String> cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(provinces);

        List<String> otherProvinces = Lists.newArrayList("广东省", "广西省");
        myService.setProvinces(otherProvinces);
        cachedProvinces = myService.getProvincesCache();
        assertThat(cachedProvinces).isEqualTo(provinces);

        val snapshotFile = getSnapshotFile("provinces");
        assertThat(snapshotFile.exists()).isTrue();
    }
}
