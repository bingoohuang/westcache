package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.peng.BasicDataCacheTest;
import com.github.bingoohuang.westcache.peng.PengTest;
import com.github.bingoohuang.westcache.springann.DemoInterfaceTest;
import com.github.bingoohuang.westcache.springann.DemoServiceTest;
import com.github.bingoohuang.westcache.springxml.ServiceSpringXmlTest;
import com.github.bingoohuang.westcache.utils.*;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.n3r.diamond.client.impl.MockDiamondServer;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/8.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BasicDataCacheTest.class,
        PengTest.class,
        DemoInterfaceTest.class,
        DemoServiceTest.class,
        AnnsTest.class,
        FastJsonsTest.class,
        AnnOverrideTest.class,
        BenchMarkTest.class,
        CacheApiTest.class,
        CacheKeyTest.class,
        CustomAnnOverrideTest.class,
        DiamondFlusherTest.class,
        DiamondManagerTest.class,
        ExceptionTest.class,
        ExpiringMapCacheManagerTest.class,
        FileCacheManagerTest.class,
        FirstTest.class,
        FlushSnapshotTest.class,
        MallCacheableTest.class,
        MySqlDictTest.class,
        RedisInterceptorTest.class,
        RedisManagerExpireCustomAnnTest.class,
        RedisManagerExpireTest.class,
        RedisManagerTest.class,
        RedisSnapshotTest.class,
        RefreshTest.class,
        SnapshotTest.class,
        SpecsTest.class,
        TableCacheFlusherTest.class,
        DurationsTest.class,
        EnvsTest.class,
        MiscTest.class,
        ServiceSpringXmlTest.class,
        WestCacheOptionTest.class,
        QuartzFlusherTest.class,
        ScheduledParserTest.class,
        WestCacheFactoryTest.class,
})
public class TestSuite {
    @ClassRule
    public static ExternalResource testRule = new ExternalResource() {
        EmbeddedRedis embeddedRedis;

        @Override
        protected void before() throws Throwable {
            MockDiamondServer.setUpMockServer();
            embeddedRedis = new EmbeddedRedis();
        }

        @Override
        protected void after() {
            MockDiamondServer.tearDownMockServer();
            embeddedRedis.stop();
        }
    };
}