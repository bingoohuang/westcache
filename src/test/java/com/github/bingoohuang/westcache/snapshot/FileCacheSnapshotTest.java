package com.github.bingoohuang.westcache.snapshot;

import com.github.bingoohuang.westcache.base.WestCacheItem;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/23.
 */
public class FileCacheSnapshotTest {
    @Test
    public void testIoException() {
        FileCacheSnapshot snapshot = new FileCacheSnapshot();
        snapshot.saveSnapshot(null, "abc",
                new WestCacheItem(Optional.of("testIoException"), null));

        new MockUp<Files>() {
            @Mock
            String toString(File file, Charset charset) throws IOException {
                throw new IOException("haha its bingoo mock it");
            }
        };

        try {
            snapshot.readSnapshot(null, "abc");
        } catch (Exception ex) {
            assertThat(ex.getClass()).isSameAs(IOException.class);
            assertThat(ex.getMessage()).isEqualTo("haha its bingoo mock it");
            return;
        }

        Assert.fail();
    }
}
