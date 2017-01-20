package com.github.bingoohuang.westcache.wuhaocheng.batchprocess;

import com.github.bingoohuang.westcache.WestCacheFactory;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.concurrent.Future;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/20.
 */
public class WuClient {
    static WuService service = WestCacheFactory.create(WuService.class);

    @Test @SneakyThrows
    public void test() {
        Future<String> token1 = service.getToken("token1");
        Future<String> token2 = service.getToken("token2");
        Future<String> token3 = service.getToken("token3");

        System.out.println("called submitted");

        assertThat(token1.get()).isNotEmpty();
        assertThat(token2.get()).isNotEmpty();
        assertThat(token3.get()).isNotEmpty();

        Future<String> token11 = service.getToken("token1");
        Future<String> token22 = service.getToken("token2");
        Future<String> token33 = service.getToken("token3");

        assertThat(token1).isSameAs(token11);
        assertThat(token2).isSameAs(token22);
        assertThat(token3).isSameAs(token33);
    }
}
