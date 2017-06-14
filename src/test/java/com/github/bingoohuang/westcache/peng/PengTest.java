package com.github.bingoohuang.westcache.peng;

import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.base.WestCacheException;
import com.github.bingoohuang.westcache.flusher.WestCacheFlusherBean;
import com.github.bingoohuang.westcache.outofbox.TableCacheFlusher;
import com.github.bingoohuang.westcache.utils.Helper;
import com.google.common.collect.Lists;
import lombok.val;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2017/1/6.
 */
public class PengTest {
    private static final TableCacheFlusher flusher = Helper.setupTableFlusherForTest();

    @BeforeClass
    public static void beforeClass() {
        service.firstPush();
    }

    @AfterClass
    public static void afterClass() {
        flusher.cancelRotateChecker();
    }

    public static final PengService service = WestCacheFactory.create(PengService.class);

    @Test
    public void getCitiesJSON() {
        val json = prepareDirectValue("PengService.getCitiesJSON_11", "full");
        String cities = service.getCitiesJSON("11");
        assertThat(cities).isEqualTo(json);
    }

    @Test
    public void getCitiesList() {
        prepareDirectValue("PengService.getCitiesList_11", "full");

        List<String> cities = service.getCitiesList("11");
        assertThat(cities).isEqualTo(Lists.newArrayList(
                "{\"code\":110,\"name\":\"beijing\"}",
                "{\"code\":111,\"name\":\"tianjin\"}"));
    }

    @Test
    public void getCityBeans() {
        prepareDirectValue("PengService.getCityBeans_11", "full");

        List<CityBean> cities = service.getCityBeans("11");
        assertThat(cities).isEqualTo(Lists.newArrayList(
                new CityBean(110, "beijing"),
                new CityBean(111, "tianjin")));
    }

    @Test
    public void getCities() {
        prepareDirectValue("PengService.getCities", "prefix");

        List<CityBean> cities11 = service.getCities("11");
        assertThat(cities11).isEqualTo(Lists.newArrayList(
                new CityBean(110, "bj"),
                new CityBean(111, "tj")));

        List<CityBean> cities22 = service.getCities("22");
        assertThat(cities22).isEqualTo(Lists.newArrayList(
                new CityBean(220, "nj"),
                new CityBean(221, "wx")));

        getCitiesNoDirectValue();
    }

    private void getCitiesNoDirectValue() {
        try {
            service.getCities("33");
        } catch (WestCacheException ex) {
            assertThat(ex.toString()).contains(
                    "cache key PengService.getCities_33 missed executable body " +
                            "in abstract method com.github.bingoohuang.westcache.peng.PengService.getCities");
            return;
        }
        Assert.fail();
    }

    @Test
    public void getKeyCities() {
        val json = prepareDirectValue("mall.city_11", "full");
        String cities = service.getKeyCities("11");
        assertThat(cities).isEqualTo(json);
    }

    private String prepareDirectValue(String prefix, String keyMatch) {
        val bean = new WestCacheFlusherBean(prefix, keyMatch, 0,
                "direct", null);
        // [{"code":110,"name":"beijing"},{"code":111,"name":"tianjin"}]
        String json =
                keyMatch.equals("full")
                        ? "[{\"code\":110,\"name\":\"beijing\"},{\"code\":111,\"name\":\"tianjin\"}]"
                        : "{\"11\":[{\"code\":110,\"name\":\"bj\"},{\"code\":111,\"name\":\"tj\"}],\"22\":[{\"code\":220,\"name\":\"nj\"},{\"code\":221,\"name\":\"wx\"}]}";

        Helper.addBeanAndUpdateDirectValue(prefix, flusher, json, bean);
        return json;
    }

}
