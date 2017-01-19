package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.keyer.SimpleKeyer;
import com.github.bingoohuang.westcache.utils.Envs;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import lombok.val;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/19.
 */
public class WestCacheConnectorTest {
    public static class ConnectorService {
        @WestCacheable(keyer = "simple")
        public long cacheMethod() {
            return System.currentTimeMillis();
        }
    }

    static ConnectorService service = WestCacheFactory.create(ConnectorService.class);

    @Test
    public void clear() {
        long l1 = service.cacheMethod();
        long l2 = service.cacheMethod();
        assertThat(l1).isEqualTo(l2);

        WestCacheConnector.clearCache(new Runnable() {
            @Override public void run() {
                service.cacheMethod();
            }
        });

        Envs.sleepMillis(10);

        long l3 = service.cacheMethod();
        long l4 = service.cacheMethod();
        assertThat(l2).isLessThan(l3);
        assertThat(l4).isEqualTo(l3);
    }

    @Test
    public void option() {
        long l1 = service.cacheMethod();
        Envs.sleepMillis(10);
        val cacheOption = WestCacheConnector.connectOption(new Runnable() {
            @Override public void run() {
                service.cacheMethod();
            }
        });
        long l2 = service.cacheMethod();
        assertThat(l1).isEqualTo(l2);

        assertThat(cacheOption).isNotNull();
        assertThat(cacheOption.getKeyer()).isInstanceOf(SimpleKeyer.class);
    }

    @Test
    public void connect() {
        long l1 = service.cacheMethod();
        WestCacheConnector.connectCache(new Runnable() {
            @Override public void run() {
                service.cacheMethod();
            }
        }, l1 + 100);

        Envs.sleepMillis(10);
        long l2 = service.cacheMethod();
        assertThat(l1 + 100).isEqualTo(l2);

        WestCacheConnector.connectCache(new Runnable() {
            @Override public void run() {
                service.cacheMethod();
            }
        }, l1 + 300);

        Envs.sleepMillis(10);
        long l3 = service.cacheMethod();
        assertThat(l1 + 300).isEqualTo(l3);
    }

    @Test
    public void key() {
        String cacheKey = WestCacheConnector.connectKey(new Runnable() {
            @Override public void run() {
                service.cacheMethod();
            }
        });

        assertThat(cacheKey).isEqualTo(
                "WestCacheConnectorTest.ConnectorService.cacheMethod");
    }

}
