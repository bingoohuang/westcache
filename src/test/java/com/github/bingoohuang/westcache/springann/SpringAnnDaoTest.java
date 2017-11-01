package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.MySqlDictTest.CacheDictBean;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import com.google.common.collect.Lists;
import lombok.experimental.var;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.truth.Truth.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringConfig.class})
public class SpringAnnDaoTest {
    @Autowired SpringAnnDao someDao;

    @Before
    public void before() {
        someDao.setup();
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
