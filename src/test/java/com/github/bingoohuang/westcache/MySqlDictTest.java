package com.github.bingoohuang.westcache;

import com.github.bingoohuang.westcache.base.WestCacheFlusher;
import com.github.bingoohuang.westcache.utils.Envs;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mockit.Capturing;
import mockit.Expectations;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.github.bingoohuang.westcache.WestCacheRegistry.FLUSHER_REGISTRY;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author bingoohuang [bingoohuang@gmail.com] Created on 2016/12/24.
 */
@Slf4j
public class MySqlDictTest {
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CacheDictBean {
        private int id;
        private String name, addr;
    }

    @EqlerConfig
    public interface DictDao {
        /*
        drop table if exists cache_dict;
        create table cache_dict(
          id int,
          name varchar(100),
          addr varchar(100),
          update_time timestamp on update current_timestamp default current_timestamp
        )engine=innodb default charset=utf8;
        insert into cache_dict(id, name, addr);
        values(1, 'bingoo', '南京'),
              (2, 'dingoo', '北京'),
              (3, 'pingoo', '上海'),
              (4, 'qingoo', '广州')");
        */

        @Sql("select id, name, addr from cache_dict")
        List<CacheDictBean> selectAll();

        @Sql("select max(update_time) max from cache_dict")
        Timestamp getMaxUpdateTime();

//        @Sql("update cache_dict set addr = #addr# where id = #id#")
//        int updateCacheDict(CacheDictBean bean);
    }

    //    static DictDao dictDao = EqlerFactory.getEqler(DictDao.class);
    static ScheduledExecutorService scheduledExecutor;
    static volatile long updateCheckTime;
    static WestCacheFlusher flusher = FLUSHER_REGISTRY.get("simple");
    private ScheduledFuture<?> scheduledFuture;

    @Capturing DictDao anyDictDao;

    @BeforeClass
    public static void beforeClass() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @Before
    public void before() {
        if (scheduledFuture != null) return;

        new Expectations() {{
            anyDictDao.getMaxUpdateTime();
            result = null;
        }};

        scheduledFuture = scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            Long lastMax = 0L;

            @Override public void run() {
                Timestamp max = dictDao.getMaxUpdateTime();
                if (max == null && lastMax == null) return;
                if (max != null && lastMax != null && max.getTime() == lastMax)
                    return;

                log.debug("lastMax:{}, max:{}, updateCheckTime:{}", lastMax, max, updateCheckTime);

                flusher.flush(null, "mysql.cache_dicts", "");
                updateCheckTime = System.currentTimeMillis();
                log.debug("update updateCheckTime:{}", updateCheckTime);
                lastMax = max == null ? null : max.getTime();
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    @AfterClass
    public static void afterClass() {
        scheduledExecutor.shutdownNow();
    }

    static DictDao dictDao = EqlerFactory.getEqler(DictDao.class);

    public static class CacheDictService {
        @WestCacheable(key = "mysql.cache_dicts", flusher = "simple")
        public List<CacheDictBean> getCacheDicts() {
            return dictDao.selectAll();
        }
    }

    @Test
    public void dictCache() {
        val service = WestCacheFactory.create(CacheDictService.class);
        val beans = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "南京"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州")
        );
        val point1 = System.currentTimeMillis();
        long lastUpdateCheckTime = updateCheckTime;

        new Expectations() {{
            anyDictDao.getMaxUpdateTime();
            result = new Timestamp(point1 + 100);
            anyDictDao.selectAll();
            result = beans;
        }};

        do {
            Envs.sleepMillis(50L);
        } while (updateCheckTime == lastUpdateCheckTime);

        val cacheDicts = service.getCacheDicts();
        assertThat(cacheDicts).isEqualTo(beans);
        val cacheDicts2 = service.getCacheDicts();
        assertThat(cacheDicts).isSameAs(cacheDicts2);

        val beans2 = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "蓝鲸"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州")
        );
        lastUpdateCheckTime = updateCheckTime;
        new Expectations() {{
            anyDictDao.getMaxUpdateTime();
            result = new Timestamp(point1 + 200);
            anyDictDao.selectAll();
            result = beans2;
        }};

        do {
            Envs.sleepMillis(50L);
        } while (updateCheckTime == lastUpdateCheckTime);

        val cacheDicts3 = service.getCacheDicts();
        assertThat(cacheDicts3).isNotSameAs(cacheDicts2);
        assertThat(cacheDicts3).isEqualTo(beans2);
    }
}
