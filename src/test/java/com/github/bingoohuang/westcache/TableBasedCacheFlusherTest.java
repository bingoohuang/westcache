package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.flusher.TableBasedCacheFlusher;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.OnErr;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;
import org.n3r.eql.eqler.annotations.SqlOptions;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class TableBasedCacheFlusherTest {
    static WestCacheFlusherDao dao = EqlerFactory.getEqler(WestCacheFlusherDao.class);
    static TableBasedCacheFlusher flusher;

    @BeforeClass
    public static void beforeClass() {
        dao.setup();
        flusher = new TableBasedCacheFlusher() {
            @Override protected List<WestCacheFlusherBean> queryAllBeans() {
                return dao.queryAllBeans();
            }
        };
        WestCacheRegistry.register("oracle", flusher);
        WestCacheRegistry.register("test", new DefaultWestCacheConfig() {
            @Override public long rotateCheckIntervalMillis() {
                return 1000;
            }
        });
    }

    @AfterClass
    public static void afterClass() {
        WestCacheRegistry.deregisterFlusher("oracle");
        WestCacheRegistry.deregisterConfig("test");
    }

    public static class TitaService {
        @WestCacheable(flusher = "oracle", keyer = "simple", config = "test")
        public String tita() {
            return "" + System.currentTimeMillis();
        }
    }

    @Test @SneakyThrows
    public void test() {
        val service = WestCacheFactory.create(TitaService.class);
        val tita1 = service.tita();

        val cacheKey = "TableBasedCacheFlusherTest.TitaService.tita";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0, "none", null);
        long lastExecuted = flusher.getLastExecuted();

        dao.addWestCacheFlusherBean(bean);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        val tita2 = service.tita();
        val tita3 = service.tita();
        assertThat(tita2).isNotEqualTo(tita1);
        assertThat(tita2).isSameAs(tita3);

        lastExecuted = flusher.getLastExecuted();
        dao.updateWestCacheFlusherBean(cacheKey);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        val tita4 = service.tita();
        val tita5 = service.tita();
        assertThat(tita4).isNotEqualTo(tita3);
        assertThat(tita4).isSameAs(tita5);
    }


    @EqlerConfig("orcl")
    public interface WestCacheFlusherDao {
        @Sql(" DROP TABLE WESTCACHE_FLUSHER;" +
                "CREATE TABLE WESTCACHE_FLUSHER(" +
                "CACHE_KEY VARCHAR2(2000 BYTE) NOT NULL," +
                "KEY_MATCH VARCHAR2(20 BYTE) DEFAULT 'full' NOT NULL," +
                "VALUE_VERSION NUMBER DEFAULT 0 NOT NULL," +
                "CACHE_STATE NUMBER DEFAULT 1 NOT NULL," +
                "VALUE_TYPE VARCHAR2(20 BYTE) DEFAULT 'none' NOT NULL," +
                "DIRECT_VALUE LONG," +
                "CONSTRAINT WESTCACHE_FLUSHER_PK PRIMARY KEY (CACHE_KEY));")
        @SqlOptions(onErr = OnErr.Resume)
        void setup();

        @Sql("SELECT CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE " +
                "FROM WESTCACHE_FLUSHER WHERE CACHE_STATE = 1")
        List<WestCacheFlusherBean> queryAllBeans();

        @Sql("SELECT DIRECT_VALUE FROM WESTCACHE_FLUSHER " +
                "WHERE CACHE_KEY = ## AND CACHE_STATE = 1")
        String getDirectValue(String key);

        @Sql("INSERT INTO WESTCACHE_FLUSHER(CACHE_KEY, KEY_MATCH, VALUE_VERSION, VALUE_TYPE) " +
                "VALUES(#?#, #?#, #?#, #?#)")
        void addWestCacheFlusherBean(WestCacheFlusherBean bean);

        @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1 " +
                "WHERE CACHE_KEY = ## AND CACHE_STATE = 1")
        void updateWestCacheFlusherBean(String key);
    }

}
