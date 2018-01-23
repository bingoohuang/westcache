package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.manager.BaseCacheManager;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/29.
 */
public class FileCacheManagerTest {
    static class FileService {
        @WestCacheable(manager = "file", keyer = "simple")
        public String executeSomething() {
            return "" + System.currentTimeMillis();
        }
    }

    @Test
    public void fileManager() {
        WestCacheOption option = WestCacheOption.builder()
                .manager("file").keyer("simple").build();
        BaseCacheManager manager = (BaseCacheManager) option.getManager();

        val cacheKey = "FileCacheManagerTest.FileService.executeSomething";
        manager.getWestCache().invalidate(option, cacheKey, null);

        val service = WestCacheFactory.create(FileService.class);
        val sth1 = service.executeSomething();
        val sth2 = service.executeSomething();

        assertThat(sth2).isEqualTo(sth1);
    }
}
