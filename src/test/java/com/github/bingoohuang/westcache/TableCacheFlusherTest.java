package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.utils.FastJsons;
import com.github.bingoohuang.westcache.utils.Helper;
import com.github.bingoohuang.westcache.utils.Redis;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.Callable;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/28.
 */
public class TableCacheFlusherTest {
    static TableCacheFlusher flusher = Helper.setupTableFlusherForTest();
    static volatile long getCitiesCalledTimes;

    public static class MyLoader implements Callable {
        @Override public Object call() {
            return "HaHa, I'm a demo only";
        }
    }

    @BeforeClass
    public static void beforeClass() {
        service.firstPush();
    }

    @AfterClass
    public static void afterClass() {
        flusher.cancelRotateChecker();
    }

    @WestCacheable(flusher = "table", keyer = "simple", snapshot = "file")
    public static abstract class TitaService {
        public String firstPush() {
            return "first";
        }

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

        public abstract String specsRedis2();
    }

    public static TitaService service = WestCacheFactory.create(TitaService.class);

    @Test @SneakyThrows
    public void tita() {
        val tita1 = service.tita();

        val cacheKey = "TableCacheFlusherTest.TitaService.tita";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0, "none", null);

        Helper.addConfigBean(flusher, bean);

        val tita2 = service.tita();
        val tita3 = service.tita();
        assertThat(tita2).isNotEqualTo(tita1);
        assertThat(tita2).isSameAs(tita3);

        Helper.upgradeVersion(cacheKey, flusher);

        val tita4 = service.tita();
        val tita5 = service.tita();
        assertThat(tita4).isNotEqualTo(tita3);
        assertThat(tita4).isSameAs(tita5);
    }

    @Test @SneakyThrows
    public void directValue() {
        val cacheKey = "TableCacheFlusherTest.TitaService.directValue";
        val bean = new WestCacheFlusherBean(cacheKey, "full", 0,
                "direct", null);

        Helper.addBeanAndUpdateDirectValue(cacheKey, flusher, "\"helllo bingoo\"", bean);

        val tita1 = service.directValue();
        assertThat(tita1).isNotEqualTo("hello bingoo");

        val tita2 = service.directValue();
        assertThat(tita2).isSameAs(tita1);
    }

    @Test @SneakyThrows
    public void getCities() {
        val prefix = "TableCacheFlusherTest.TitaService.getCities";
        val bean = new WestCacheFlusherBean(prefix, "prefix", 0,
                "none", null);
        Helper.addConfigBean(flusher, bean);

        String jiangSuCities1 = service.getCities("JiangSu");
        String jiangXiCities1 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(2);
        assertThat(jiangSuCities1).isNotEqualTo(jiangXiCities1);

        String jiangSuCities11 = service.getCities("JiangSu");
        String jiangXiCities11 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(2);
        assertThat(jiangSuCities11).isSameAs(jiangSuCities1);
        assertThat(jiangXiCities11).isSameAs(jiangXiCities1);

        Helper.upgradeVersion(prefix, flusher);

        String jiangSuCities2 = service.getCities("JiangSu");
        String jiangXiCities2 = service.getCities("JiangXi");
        assertThat(getCitiesCalledTimes).isEqualTo(4);
        assertThat(jiangSuCities2).isNotEqualTo(jiangSuCities1);
        assertThat(jiangXiCities2).isNotEqualTo(jiangXiCities1);
    }

    @Test @SneakyThrows
    public void getCitiesWithDirectValue() {
        val prefix = "TableCacheFlusherTest.TitaService.getCities2";
        val bean = new WestCacheFlusherBean(prefix, "prefix", 0,
                "direct", null);

        String json = FastJsons.json(ImmutableMap.of(
                "JiangSu", "XXX",
                "JiangXi", "YYY"));
        Helper.addBeanAndUpdateDirectValue(prefix, flusher, json, bean);

        String jiangSuCities1 = service.getCities2("JiangSu");
        String jiangXiCities1 = service.getCities2("JiangXi");
        assertThat(jiangSuCities1).isEqualTo("XXX");
        assertThat(jiangXiCities1).isEqualTo("YYY");

        String jiangSuCities11 = service.getCities2("JiangSu");
        String jiangXiCities11 = service.getCities2("JiangXi");
        assertThat(jiangSuCities11).isSameAs(jiangSuCities1);
        assertThat(jiangXiCities11).isSameAs(jiangXiCities1);

        Helper.upgradeVersion(prefix, flusher);

        String jiangSuCities2 = service.getCities2("JiangSu");
        String jiangXiCities2 = service.getCities2("JiangXi");
        assertThat(jiangSuCities2).isNotSameAs(jiangSuCities1);
        assertThat(jiangXiCities2).isNotSameAs(jiangXiCities1);

        String json2 = FastJsons.json(ImmutableMap.of(
                "JiangSu", "XXX111",
                "JiangXi", "YYY222"));
        Helper.updateDirectValue(prefix, flusher, json2);

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
        val prefix = "TableCacheFlusherTest.TitaService.specs";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "direct", "readBy=loader,loaderClass=com.github.bingoohuang.westcache.TableCacheFlusherTest$MyLoader");

        Helper.addConfigBean(flusher, bean);

        val r1 = service.specs();
        assertThat(r1).isEqualTo("HaHa, I'm a demo only");
    }

    @Test @SneakyThrows
    public void specsRedis() {
        val prefix = "TableCacheFlusherTest.TitaService.specsRedis";
        val bean = new WestCacheFlusherBean(prefix, "full", 0,
                "direct", "readBy=redis");

        Redis.getJedis().set(Redis.PREFIX + prefix, "\"I am redis body\"");

        Helper.addConfigBean(flusher, bean);

        val r1 = service.specsRedis();
        assertThat(r1).isEqualTo("I am redis body");

        Helper.disableConfigBean(flusher, bean);
        try {
            service.specsRedis();

        } catch (Throwable ex) {
            assertThat(ex.getMessage()).isEqualTo(
                    "cache key TableCacheFlusherTest.TitaService.specsRedis " +
                            "missed executable body in abstract method " +
                            "com.github.bingoohuang.westcache.TableCacheFlusherTest$TitaService.specsRedis");
            return;
        }
        Assert.fail();
    }
}
