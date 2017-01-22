package com.github.bingoohuang.westcache.batch;

import com.github.bingoohuang.westcache.WestCacheFactory;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class BatchTest {
    static BatchService service = WestCacheFactory.create(BatchService.class);

    @Test @SneakyThrows
    public void test() {
        Future<String> token1 = service.getToken("token1");
        Future<String> token2 = service.getToken("token2");
        Future<String> token3 = service.getToken("token3");

        assertNotEmpty(token1, token2, token3);

        Future<String> token11 = service.getToken("token1");
        Future<String> token22 = service.getToken("token2");
        Future<String> token33 = service.getToken("token3");

        assertSame(token1, token2, token3, token11, token22, token33);
    }


    @Test @SneakyThrows
    public void test2() {
        Future<String> token1 = service.getToken2("token1");
        Future<String> token2 = service.getToken2("token2");
        Future<String> token3 = service.getToken2("token3");

        assertNotEmpty(token1, token2, token3);

        Future<String> token11 = service.getToken2("token1");
        Future<String> token22 = service.getToken2("token2");
        Future<String> token33 = service.getToken2("token3");

        assertSame(token1, token2, token3, token11, token22, token33);
    }

    @Test @SneakyThrows
    public void test3() {
        Future<String> token1 = service.getToken3("bad");
        Future<String> token2 = service.getToken3("token2");
        Future<String> token3 = service.getToken3("token3");

        assertEx(token3, "result is not available");
    }

    @Test @SneakyThrows
    public void test4() {
        Future<String> token1 = service.getToken4("bad");
        Future<String> token2 = service.getToken4("token2");
        Future<String> token3 = service.getToken4("token3");

        assertEx(token1, "dingoo here");
        assertEx(token2, "dingoo here");
        assertEx(token3, "dingoo here");
    }

    @SneakyThrows
    private void assertEx(Future<String> token, String expected) {
        try {
            token.get();
        } catch (ExecutionException ex) {
            assertThat(ex.getCause().getMessage()).isEqualTo(expected);
            return;
        }
        Assert.fail();
    }

    private void assertSame(Future<String> token1,
                            Future<String> token2,
                            Future<String> token3,
                            Future<String> token11,
                            Future<String> token22,
                            Future<String> token33) {
        assertThat(token1).isSameAs(token11);
        assertThat(token2).isSameAs(token22);
        assertThat(token3).isSameAs(token33);
    }

    @SneakyThrows
    private void assertNotEmpty(Future<String>... tokens) {
        for (val token : tokens)
            assertThat(token.get()).isNotEmpty();
    }
}
