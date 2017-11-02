package com.github.bingoohuang.westcache.eqler;

import com.github.bingoohuang.westcache.MySqlDictTest.CacheDictBean;
import com.github.bingoohuang.westcache.springann.SpringAnnDao;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.google.common.collect.Lists;
import lombok.experimental.var;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;

import static com.google.common.truth.Truth.assertThat;

public class EqlerCacheableTest {
    static SpringAnnDao someDao;

    @BeforeClass
    public static void beforeClass() {
        someDao = EqlerFactory.getEqler(SpringAnnDao.class);
        someDao.setup();
        WestCacheConnector.clearCache(new Runnable() {
            @Override public void run() {
                someDao.selectAll();
            }
        });
    }

    @Test
    public void test() {
        var beans = someDao.selectAll();
        var lists = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "南京"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州"));
        assertThat(beans).containsExactlyElementsIn(lists);

        someDao.updateCacheDict(new CacheDictBean(1, "bingoo", "蓝鲸"));

        beans = someDao.selectAll();
        assertThat(beans).containsExactlyElementsIn(lists);

        WestCacheConnector.clearCache(new Runnable() {
            @Override public void run() {
                someDao.selectAll();
            }
        });

        var lists2 = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "蓝鲸"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州"));

        beans = someDao.selectAll();
        assertThat(beans).containsExactlyElementsIn(lists2);
    }
}
