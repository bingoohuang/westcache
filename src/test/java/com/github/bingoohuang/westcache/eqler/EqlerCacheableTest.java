package com.github.bingoohuang.westcache.eqler;

import com.github.bingoohuang.westcache.MySqlDictTest.CacheDictBean;
import com.github.bingoohuang.westcache.WestCacheFactory;
import com.github.bingoohuang.westcache.WestCacheable;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.google.common.collect.Lists;
import lombok.experimental.var;
import lombok.val;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class EqlerCacheableTest {
    @EqlerConfig
    public interface SomeDao {
        @Sql({"drop table if exists cache_dict",
                "create table cache_dict(id int,  name varchar(100),  addr varchar(100))",
                "insert into cache_dict(id, name, addr)"
                        + "values(1, 'bingoo', '南京'), (2, 'dingoo', '北京'), (3, 'pingoo', '上海'), (4, 'qingoo', '广州')"})
        void setup();

        @WestCacheable
        @Sql("select id, name, addr from cache_dict")
        List<CacheDictBean> selectAll();

        @Sql("update cache_dict set addr = #addr# where id = #id#")
        int updateCacheDict(CacheDictBean bean);
    }

    static SomeDao someDao;

    @BeforeClass
    public static void beforeClass() {
        val dao = EqlerFactory.getEqler(SomeDao.class);
        dao.setup();

        someDao = WestCacheFactory.create(dao);
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
