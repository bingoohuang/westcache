package com.github.bingoohuang.westcache.springann;

import com.github.bingoohuang.westcache.MySqlDictTest.CacheDictBean;
import com.github.bingoohuang.westcache.WestCacheable;
import org.n3r.eql.eqler.annotations.EqlerConfig;
import org.n3r.eql.eqler.annotations.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@EqlerConfig
public interface SpringAnnDao {
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @WestCacheable(manager = "redis") @interface YogaCacheable {
    }


    @Sql({"drop table if exists cache_dict",
            "create table cache_dict(id int,  name varchar(100),  addr varchar(100))",
            "insert into cache_dict(id, name, addr)"
                    + "values(1, 'bingoo', '南京'), (2, 'dingoo', '北京'), (3, 'pingoo', '上海'), (4, 'qingoo', '广州')"})
    void setup();

    @Sql({"drop table if exists cache_dict",
            "create table cache_dict(id int,  name varchar(100),  addr varchar(100))",
            "insert into cache_dict(id, name, addr) values(1, 'bingoo', '南京')"})
    void setup1();

    @YogaCacheable
    @Sql("select id, name, addr from cache_dict")
    List<CacheDictBean> selectAll();

    @Sql("update cache_dict set addr = #addr# where id = #id#")
    int updateCacheDict(CacheDictBean bean);
}
