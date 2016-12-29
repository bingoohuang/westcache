package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.config.DefaultWestCacheConfig;
import com.github.bingoohuang.westcache.dao.WestCacheFlusherDao;
import com.github.bingoohuang.westcache.flusher.TableBasedCacheFlusher;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.util.Conf;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class TableBasedCacheFlusherTest {
    static WestCacheFlusherDao dao = EqlerFactory.getEqler(WestCacheFlusherDao.class);
    static TableBasedCacheFlusher flusher;
    static volatile long lastReadDirectValue;
    static volatile long getCitiesCalledTimes;

    public interface Loader {
        Object load();
    }

    public static class MyLoader implements Loader {
        @Override public Object load() {
            return "HaHa, I'm a demo only";
        }
    }

    static Jedis jedis = new Jedis(Conf.REDIS_HOST, Conf.REDIS_PORT);

    @BeforeClass
    public static void beforeClass() {
        dao.setup();
        flusher = new TableBasedCacheFlusher() {
            @Override protected List<WestCacheFlusherBean> queryAllBeans() {
                return dao.queryAllBeans();
            }

            @Override @SneakyThrows
            protected Object readDirectValue(final WestCacheFlusherBean bean) {
                lastReadDirectValue = System.currentTimeMillis();

                String specs = bean.getSpecs();
                if (isNotBlank(specs)) {
                    val splitter = Splitter.on(';').withKeyValueSeparator('=');
                    val specsMap = splitter.split(specs);
                    val loaderClass = specsMap.get("loaderClass");
                    if (isNotBlank(loaderClass)) {
                        Class<?> clazz = Class.forName(loaderClass);
                        val loader = (Loader) clazz.newInstance();
                        return loader.load();
                    }

                    val directRedis = specsMap.get("directRedis");
                    if ("yes".equalsIgnoreCase(directRedis)) {
                        val value = jedis.get(bean.getCacheKey());
                        if (isNotBlank(value)) {
                            return FastJsons.parse(value);
                        }
                    }
                }

                String directJson = dao.getDirectValue(bean.getCacheKey());
                if (isBlank(directJson)) return null;
                return FastJsons.parse(directJson);
            }

        };
        WestCacheRegistry.register("table", flusher);
        WestCacheRegistry.register("test", new DefaultWestCacheConfig() {
            @Override public long rotateCheckIntervalMillis() {
                return 1000;
            }
        });
    }

    @AfterClass
    public static void afterClass() {
        jedis.close();
        WestCacheRegistry.deregisterFlusher("table");
        WestCacheRegistry.deregisterConfig("test");
    }

    @WestCacheable(flusher = "table", keyer = "simple", config = "test")
    public static abstract class TitaService {
        public String tita() {
            return "" + System.currentTimeMillis();
        }

        public abstract String directValue();

        public String getCities(String provinceCode) {
            ++getCitiesCalledTimes;
            return provinceCode + System.currentTimeMillis();
        }

        public abstract String getCities2(String provinceCode);

        public abstract String specs();

        public abstract String specsRedis();
    }

    @Test @SneakyThrows
    public void tita() {
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

    @Test @SneakyThrows
    public void directValue() {
        val service = WestCacheFactory.create(TitaService.class);

        val cacheKey = "TableBasedCacheFlusherTest.TitaService.directValue";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0,
                "direct", null);

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
                "none", null);

        long lastExecuted = flusher.getLastExecuted();
        dao.addWestCacheFlusherBean(bean);
        service.getCities("JiangSu");

        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities1 = service.getCities("JiangSu");
        String jiangXiCities1 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(3);
        assertThat(jiangSuCities1).isNotEqualTo(jiangXiCities1);

        String jiangSuCities11 = service.getCities("JiangSu");
        String jiangXiCities11 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(3);
        assertThat(jiangSuCities11).isSameAs(jiangSuCities1);
        assertThat(jiangXiCities11).isSameAs(jiangXiCities1);

        lastExecuted = flusher.getLastExecuted();
        dao.updateWestCacheFlusherBean(prefix);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities2 = service.getCities("JiangSu");
        String jiangXiCities2 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(5);
        assertThat(jiangSuCities2).isNotEqualTo(jiangSuCities1);
        assertThat(jiangXiCities2).isNotEqualTo(jiangXiCities1);
    }

    @Test @SneakyThrows
    public void getCitiesWithDirectValue() {
        val service = WestCacheFactory.create(TitaService.class);

        val prefix = "TableBasedCacheFlusherTest.TitaService.getCities2";
        val bean = new WestCacheFlusherBean(prefix, "prefix", 0,
                "direct", null);

        Map<String, String> directValue = Maps.newHashMap();
        directValue.put("JiangSu", "XXX");
        directValue.put("JiangXi", "YYY");
        String json = FastJsons.json(directValue);

        long lastExecuted = flusher.getLastExecuted();
        dao.addWestCacheFlusherBean(bean);
        dao.updateDirectValue(prefix, json);
        service.tita(); // just to make sure that the rotating check thread running
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities1 = service.getCities2("JiangSu");
        String jiangXiCities1 = service.getCities2("JiangXi");
        assertThat(jiangSuCities1).isEqualTo("XXX");
        assertThat(jiangXiCities1).isEqualTo("YYY");

        String jiangSuCities11 = service.getCities2("JiangSu");
        String jiangXiCities11 = service.getCities2("JiangXi");
        assertThat(jiangSuCities11).isSameAs(jiangSuCities1);
        assertThat(jiangXiCities11).isSameAs(jiangXiCities1);

        lastExecuted = flusher.getLastExecuted();
        dao.updateWestCacheFlusherBean(prefix);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        String jiangSuCities2 = service.getCities2("JiangSu");
        String jiangXiCities2 = service.getCities2("JiangXi");
        assertThat(jiangSuCities2).isNotSameAs(jiangSuCities1);
        assertThat(jiangXiCities2).isNotSameAs(jiangXiCities1);

        Map<String, String> directValue2 = Maps.newHashMap();
        directValue2.put("JiangSu", "XXX111");
        directValue2.put("JiangXi", "YYY222");
        String json2 = FastJsons.json(directValue2);

        lastExecuted = flusher.getLastExecuted();
        dao.updateDirectValue(prefix, json2);
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);


        String jiangSuCitiesA = service.getCities2("JiangSu");
        String jiangXiCitiesA = service.getCities2("JiangXi");
        assertThat(jiangSuCitiesA).isEqualTo("XXX111");
        assertThat(jiangXiCitiesA).isEqualTo("YYY222");

        String jiangSuCitiesA1 = service.getCities2("JiangSu");
        String jiangXiCitiesA1 = service.getCities2("JiangXi");
        assertThat(jiangSuCitiesA1).isSameAs(jiangSuCitiesA);
        assertThat(jiangXiCitiesA1).isSameAs(jiangXiCitiesA);
    }

    @Test @SneakyThrows
    public void specs() {
        val service = WestCacheFactory.create(TitaService.class);

        val prefix = "TableBasedCacheFlusherTest.TitaService.specs";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "direct", "loaderClass=com.github.bingoohuang.westcache.TableBasedCacheFlusherTest$MyLoader");

        long lastExecuted = flusher.getLastExecuted();
        dao.addWestCacheFlusherBean(bean);
        service.tita(); // just to make sure that the rotating check thread running
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        val r1 = service.specs();
        assertThat(r1).isEqualTo("HaHa, I'm a demo only");
    }

    @Test @SneakyThrows
    public void specsRedis() {
        val service = WestCacheFactory.create(TitaService.class);

        val prefix = "TableBasedCacheFlusherTest.TitaService.specsRedis";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "direct", "directRedis=yes");

        jedis.set(prefix, "\"I am redis body\"");

        long lastExecuted = flusher.getLastExecuted();
        dao.addWestCacheFlusherBean(bean);
        service.tita(); // just to make sure that the rotating check thread running
        do {
            Thread.sleep(100L);
        } while (flusher.getLastExecuted() == lastExecuted);

        val r1 = service.specsRedis();
        assertThat(r1).isEqualTo("I am redis body");
    }
}
