package com.github.bingoohuang.westcache.flusher;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import lombok.val;
import mockit.Deencapsulation;
import org.junit.Test;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.impl.DiamondSubscriber;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/25.
 */
public class DiamondCacheFlusherTest {
    public static class DiamondFlusherService {
        @WestCacheable(flusher = "diamond", specs = "static.key=yes")
        public long getBigData() {
            return System.currentTimeMillis();
        }
    }

    static DiamondFlusherService service = WestCacheFactory.create(DiamondFlusherService.class);
    static String cacheKey = WestCacheConnector.connectKey(new Runnable() {
        @Override public void run() {
            service.getBigData();
        }
    });

    @Test
    public void acceptChange() {
        long l = System.currentTimeMillis();
        long first = service.getBigData();
        assertThat(first).isGreaterThan(l);

        val diamondSubscriber = DiamondSubscriber.getInstance();
        val diamondRemoteChecker = Deencapsulation.getField(diamondSubscriber, "diamondRemoteChecker");
        val diamondAllListener = Deencapsulation.getField(diamondRemoteChecker, "diamondAllListener");
        val diamondStone = new DiamondStone();
        diamondStone.setDiamondAxis(new DiamondAxis(DiamondCacheFlusher.GROUP, cacheKey));
        diamondStone.setContent("xxxx");
        Deencapsulation.invoke(diamondAllListener, "accept", diamondStone);

        assertThat(service.getBigData()).isGreaterThan(first);
    }
}
