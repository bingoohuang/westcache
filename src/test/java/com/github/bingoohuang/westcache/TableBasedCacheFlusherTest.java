package com.github.bingoohuang.westcache;

import com.alibaba.fastjson.JSON;
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
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class TableBasedCacheFlusherTest {
    static WestCacheFlusherDao dao = EqlerFactory.getEqler(WestCacheFlusherDao.class);
    static TableBasedCacheFlusher flusher;
    static volatile long lastReadDirectValue;
    static volatile long getCitiesCalledTimes;

    @BeforeClass
    public static void beforeClass() {
        dao.setup();
        flusher = new TableBasedCacheFlusher() {
            @Override protected List<WestCacheFlusherBean> queryAllBeans() {
                return dao.queryAllBeans();
            }

            @Override
            protected Object readDirectValue(String cacheKey, String subKey) {
                lastReadDirectValue = System.currentTimeMillis();

                String directJson = dao.getDirectValue(cacheKey);
                if (isBlank(directJson)) return null;

                if (subKey == null) return JSON.parse(directJson);

                return JSON.parseObject(directJson).get(subKey);
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

    public static abstract class TitaService {
        @WestCacheable(flusher = "oracle", keyer = "simple", config = "test")
        public String tita() {
            return "" + System.currentTimeMillis();
        }

        @WestCacheable(flusher = "oracle", keyer = "simple", config = "test")
        public abstract String directValue();

        @WestCacheable(flusher = "oracle", keyer = "simple", config = "test",
                snapshot = "file")
        public String getCities(String provinceCode) {
            ++getCitiesCalledTimes;
            return provinceCode + System.currentTimeMillis();
        }
    }

    @Test @SneakyThrows
    public void tita() {
        val service = WestCacheFactory.create(TitaService.class);
        val tita1 = service.tita();

        val cacheKey = "TableBasedCacheFlusherTest.TitaService.tita";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0, "none");

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

    @Test @SneakyThrows
    public void directValue() {
        val service = WestCacheFactory.create(TitaService.class);

        val cacheKey = "TableBasedCacheFlusherTest.TitaService.directValue";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0,
                "direct");

        dao.addWestCacheFlusherBean(bean);
        dao.updateDirectValue(cacheKey, "\"helllo bingoo\"");

        val tita1 = service.directValue();
        assertThat(tita1).isNotEqualTo("hello bingoo");

        val last1 = lastReadDirectValue;
        val tita2 = service.directValue();
        assertThat(tita2).isSameAs(tita1);
        assertThat(last1).isEqualTo(lastReadDirectValue);
    }

    @Test @SneakyThrows
    public void getCities() {
        val service = WestCacheFactory.create(TitaService.class);

        val prefix = "TableBasedCacheFlusherTest.TitaService.getCities";
        val bean = new WestCacheFlusherBean(prefix, "prefix", 0,
                "none");

        long lastExecuted = flusher.getLastExecuted();
        dao.addWestCacheFlusherBean(bean);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities1 = service.getCities("JiangSu");
        String jiangXiCities1 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(2);
        assertThat(jiangSuCities1).isNotEqualTo(jiangXiCities1);

        String jiangSuCities11 = service.getCities("JiangSu");
        String jiangXiCities11 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(2);
        assertThat(jiangSuCities11).isSameAs(jiangSuCities1);
        assertThat(jiangXiCities11).isSameAs(jiangXiCities1);

        lastExecuted = flusher.getLastExecuted();
        dao.updateWestCacheFlusherBean(prefix);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities2 = service.getCities("JiangSu");
        String jiangXiCities2 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(4);
        assertThat(jiangSuCities2).isNotEqualTo(jiangSuCities1);
        assertThat(jiangXiCities2).isNotEqualTo(jiangXiCities1);
    }


    @EqlerConfig("mysql")
    public interface WestCacheFlusherDao {
        @Sql("   DROP TABLE IF EXISTS WESTCACHE_FLUSHER;" +
                "CREATE TABLE WESTCACHE_FLUSHER(" +
                "   CACHE_KEY VARCHAR(2000) NOT NULL PRIMARY KEY COMMENT 'cache key'," +
                "   KEY_MATCH VARCHAR(20) DEFAULT 'full' NOT NULL COMMENT 'full:full match,filename:file name match, regex:regex match ant:ant path match'," +
                "   VALUE_VERSION TINYINT DEFAULT 0 NOT NULL COMMENT 'version of cache, increment it to update cache'," +
                "   CACHE_STATE TINYINT DEFAULT 1 NOT NULL COMMENT 'direct json value for the cache'," +
                "   VALUE_TYPE VARCHAR(20) DEFAULT 'none' NOT NULL COMMENT 'value access type, direct: use direct json in DIRECT_VALUE field'," +
                "   DIRECT_VALUE TEXT" +
                ") ;")
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

        @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1," +
                "DIRECT_VALUE = #2# " +
                "WHERE CACHE_KEY = #1#")
        int updateDirectValue(String cacheKye, String directValue);

        @Sql("UPDATE WESTCACHE_FLUSHER SET VALUE_VERSION = VALUE_VERSION + 1 " +
                "WHERE CACHE_KEY = ##")
        void updateWestCacheFlusherBean(String key);
    }

}
