package com.github.bingoohuang.westcache;

import com.google.common.collect.Lists;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.eql.eqler.EqlerFactory;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    @EqlerConfig("mysql")
    public interface DictDao {
        @Sql("drop table if exists cache_dict;" +

                "create table cache_dict(" +
                "  id int, " +
                "  name varchar(100), " +
                "  addr varchar(100), " +
                "  update_time timestamp on update current_timestamp default current_timestamp" +
                ")engine=innodb default charset=utf8;" +

                "insert into cache_dict(id, name, addr) " +
                "values(1, 'bingoo', '南京')," +
                "      (2, 'dingoo', '北京')," +
                "      (3, 'pingoo', '上海')," +
                "      (4, 'qingoo', '广州')")
        void setup();

        @Sql("select id, name, addr from cache_dict")
        List<CacheDictBean> selectAll();

        @Sql("select max(update_time) max from cache_dict")
        Timestamp getMaxUpdateTime();

        @Sql("select now()")
        Timestamp selectNow();

        @Sql("update cache_dict set addr = #addr# where id = #id#")
        int updateCacheDict(CacheDictBean bean);
    }

    static DictDao dictDao = EqlerFactory.getEqler(DictDao.class);
    static ScheduledExecutorService scheduledExecutor;
    static volatile long updateCheckTime;
    static volatile long setupTimeSeconds;

    @BeforeClass
    public static void beforeClass() {
        dictDao.setup();
        setupTimeSeconds = dictDao.selectNow().getTime() / 1000;

        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(new Runnable() {
            Long lastMax = 0L;

            @Override public void run() {
                Timestamp max = dictDao.getMaxUpdateTime();
                if (max == null && lastMax == null) return;
                if (max != null && max.getTime() == lastMax) return;

                log.debug("lastMax:{}, max:{}, updateCheckTime:{}", lastMax, max, updateCheckTime);

                if (lastMax != null && lastMax > 0L) {
                    val flusher = WestCacheRegistry.getFlusher("simple");
                    flusher.flush("mysql.cache_dicts");
                    updateCheckTime = System.currentTimeMillis();
                    log.debug("update updateCheckTime:{}", updateCheckTime);
                }

                lastMax = max == null ? null : max.getTime();
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    @AfterClass
    public static void afterClass() {
        scheduledExecutor.shutdownNow();
    }

    public static class CacheDictService {
        @WestCacheable(key = "mysql.cache_dicts", flusher = "simple")
        public List<CacheDictBean> getCacheDicts() {
            return dictDao.selectAll();
        }
    }

    @Test @SneakyThrows
    public void dictCache() {
        val service = WestCacheFactory.create(CacheDictService.class);

        val cacheDicts = service.getCacheDicts();
        val beans = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "南京"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州")
        );
        assertThat(cacheDicts).isEqualTo(beans);

        val cacheDicts2 = service.getCacheDicts();
        assertThat(cacheDicts).isSameAs(cacheDicts2);

        // 数据库里面的时间，精度只到秒，所以这里必须确保更新时，时间已经过了1秒
        do {
            Thread.sleep(50L);
        } while (dictDao.selectNow().getTime() / 1000 <= setupTimeSeconds);

        long lastUpdateCheckTime = updateCheckTime;
        dictDao.updateCacheDict(new CacheDictBean(1, "bingoo", "蓝鲸"));

        do {
            Thread.sleep(50);
        } while (lastUpdateCheckTime == updateCheckTime);

        val cacheDicts3 = service.getCacheDicts();
        assertThat(cacheDicts3).isNotSameAs(cacheDicts2);

        val beans3 = Lists.newArrayList(
                new CacheDictBean(1, "bingoo", "蓝鲸"),
                new CacheDictBean(2, "dingoo", "北京"),
                new CacheDictBean(3, "pingoo", "上海"),
                new CacheDictBean(4, "qingoo", "广州")
        );
        assertThat(cacheDicts3).isEqualTo(beans3);
    }
}
