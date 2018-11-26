package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.MySqlDictTest.CacheDictBean;
import com.github.bingoohuang.westcache.utils.WestCacheConnector;
import lombok.val;
import lombok.var;
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
        someDao.setup1();
    }

    @Test
    public void test() {
        WestCacheConnector.clearCache(() -> someDao.selectAll());

        var beans = someDao.selectAll();
        var bean1 = new CacheDictBean(1, "bingoo", "南京");
        assertThat(beans).containsExactly(bean1);

        val bean2 = new CacheDictBean(1, "bingoo", "蓝鲸");
        someDao.updateCacheDict(bean2);

        beans = someDao.selectAll();
        assertThat(beans).containsExactly(bean1);

        WestCacheConnector.clearCache(() -> someDao.selectAll());

        beans = someDao.selectAll();
        assertThat(beans).containsExactly(bean2);
    }
}
