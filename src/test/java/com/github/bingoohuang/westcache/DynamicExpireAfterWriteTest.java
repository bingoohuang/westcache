package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.ExpireAfterWritable;
import com.github.bingoohuang.westcache.base.ExpireAfterWrite;
import com.github.bingoohuang.westcache.utils.Envs;
import lombok.*;
import org.junit.Test;

import java.util.Random;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public class DynamicExpireAfterWriteTest {
    public static class DyanmicExpireService {
        @Setter private long timestamp;
        @Setter private long timestamp2;

        @WestCacheable(manager = "redis")
        public ServiceResult getToken() {
            return new ServiceResult(timestamp,
                    1 + new Random().nextInt(5));
        }

        @WestCacheable(manager = "redis")
        public ServiceResult2 getToken2() {
            return new ServiceResult2(timestamp2,
                    1 + new Random().nextInt(5));
        }
    }

    @NoArgsConstructor @AllArgsConstructor @Data
    public static class ServiceResult implements ExpireAfterWritable {
        private long timestamp;
        private int expireAfterWriteSeconds;

        @Override public String expireAfterWrite() {
            return expireAfterWriteSeconds + "s";
        }
    }


    @NoArgsConstructor @AllArgsConstructor @Data
    public static class ServiceResult2 {
        private long timestamp;
        private int expireAfterWriteSeconds;

        @ExpireAfterWrite
        public String expireAfterWrite() {
            return expireAfterWriteSeconds + "s";
        }
    }

    @Test
    public void dynamicExpire() {
        val service = WestCacheFactory.create(DyanmicExpireService.class);
        service.setTimestamp(1);
        ServiceResult token = service.getToken();
        assertThat(token.getTimestamp()).isEqualTo(1L);

        ServiceResult token2 = service.getToken();
        assertThat(token2.getTimestamp()).isEqualTo(1L);

        int seconds = token.getExpireAfterWriteSeconds();
        service.setTimestamp(2);
        Envs.sleepMillis(seconds * 1000);
        ServiceResult token3 = service.getToken();
        assertThat(token3.getTimestamp()).isEqualTo(2L);
    }

    @Test
    public void dynamicExpire2() {
        val service = WestCacheFactory.create(DyanmicExpireService.class);
        service.setTimestamp2(1);
        ServiceResult2 token = service.getToken2();
        assertThat(token.getTimestamp()).isEqualTo(1L);

        ServiceResult2 token2 = service.getToken2();
        assertThat(token2.getTimestamp()).isEqualTo(1L);

        int seconds = token2.getExpireAfterWriteSeconds();
        service.setTimestamp2(2);
        Envs.sleepMillis(seconds * 1000);
        ServiceResult2 token3 = service.getToken2();
        assertThat(token3.getTimestamp()).isEqualTo(2L);
    }
}
